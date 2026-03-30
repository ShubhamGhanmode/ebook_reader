package com.shubhamghanmode.inkfold.feature.reader.outline

import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.epub.landmarks
import org.readium.r2.shared.publication.epub.pageList

@OptIn(ExperimentalReadiumApi::class)
object ReaderOutlineMapper {
    data class Result(
        val sections: List<ReaderOutlineSection>,
        val locatorsByItemId: Map<String, Locator>
    )

    fun map(publication: Publication): Result {
        val locatorsByItemId = linkedMapOf<String, Locator>()

        val sections = buildList {
            buildSection(
                publication = publication,
                links = publication.tableOfContents,
                sectionId = ReaderOutlineSectionId.CONTENTS,
                locatorsByItemId = locatorsByItemId
            )?.let(::add)

            buildSection(
                publication = publication,
                links = publication.pageList,
                sectionId = ReaderOutlineSectionId.PAGES,
                locatorsByItemId = locatorsByItemId
            )?.let(::add)

            buildSection(
                publication = publication,
                links = publication.landmarks,
                sectionId = ReaderOutlineSectionId.LANDMARKS,
                locatorsByItemId = locatorsByItemId
            )?.let(::add)
        }

        return Result(
            sections = sections,
            locatorsByItemId = locatorsByItemId
        )
    }

    private fun buildSection(
        publication: Publication,
        links: List<Link>,
        sectionId: ReaderOutlineSectionId,
        locatorsByItemId: MutableMap<String, Locator>
    ): ReaderOutlineSection? {
        val items = flattenLinks(
            links = links,
            publication = publication,
            locatorsByItemId = locatorsByItemId
        )

        return items
            .takeIf(List<ReaderOutlineItem>::isNotEmpty)
            ?.let { ReaderOutlineSection(id = sectionId, items = it) }
    }

    private fun flattenLinks(
        links: List<Link>,
        publication: Publication,
        locatorsByItemId: MutableMap<String, Locator>,
        depth: Int = 0
    ): List<ReaderOutlineItem> =
        buildList {
            links.forEachIndexed { index, link ->
                val locator = publication.locatorFromLink(link) ?: return@forEachIndexed
                val itemId = buildString {
                    append(link.href)
                    append('#')
                    append(depth)
                    append('-')
                    append(index)
                }

                locatorsByItemId[itemId] = locator
                add(
                    ReaderOutlineItem(
                        id = itemId,
                        title = link.title ?: link.href.toString(),
                        depth = depth
                    )
                )
                addAll(
                    flattenLinks(
                        links = link.children,
                        publication = publication,
                        locatorsByItemId = locatorsByItemId,
                        depth = depth + 1
                    )
                )
            }
        }
}
