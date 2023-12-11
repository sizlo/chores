package com.timsummertonbrier.chores.service

import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.AttributeProvider
import org.commonmark.renderer.html.HtmlRenderer

class MarkdownToHtmlParser {
    fun parse(src: String): String {
        return render(doParse(src))
    }

    private fun doParse(src: String): Node {
        return Parser.builder().build().parse(src)
    }

    private fun render(document: Node): String {
        return HtmlRenderer
            .builder()
            .attributeProviderFactory { NewTabLinkAttributeProvider() }
            .build()
            .render(document)
    }

    private class NewTabLinkAttributeProvider : AttributeProvider {
        override fun setAttributes(node: Node, tagName: String, attributes: MutableMap<String, String>) {
            if (node is Link) {
                attributes["target"] = "_blank"
            }
        }
    }
}