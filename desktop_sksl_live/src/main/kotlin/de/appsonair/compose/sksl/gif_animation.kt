package de.appsonair.compose.sksl

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOInvalidTreeException
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.ImageOutputStream


interface GifAnimationDsl {
    fun add(image: ImageBitmap)
}

fun createGifAnimation(
    file: File,
    sampleImage: ImageBitmap,
    delayTicks: Int = 1, //1 second do have 100 ticks
    loop: Boolean = true,
    block: GifAnimationDsl.() -> Unit
) {
    FileImageOutputStream(file).use { output ->
        val img = sampleImage.toAwtImage()
        val writer = GifSequenceWriter(output, img.type, delayTicks, loop)
        val dsl = object : GifAnimationDsl {
            override fun add(image: ImageBitmap) {
                writer.writeToSequence(image.toAwtImage())
            }
        }
        block(dsl)
        writer.close()
    }
}

class GifSequenceWriter(out: ImageOutputStream, imageType: Int, delay: Int, loop: Boolean) {
    private val writer = ImageIO.getImageWritersBySuffix("gif").next()
    private val params = writer.defaultWriteParam

    private val imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType)
    private val metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, params)

    init {
        writer.output = out
        writer.prepareWriteSequence(null)
        configureRootMetadata(delay, loop)
    }

    @Throws(IIOInvalidTreeException::class)
    private fun configureRootMetadata(delay: Int, loop: Boolean) {
        val metaFormatName = metadata.nativeMetadataFormatName
        val root = metadata.getAsTree(metaFormatName) as IIOMetadataNode
        getNode(root, "GraphicControlExtension").apply {
            setAttribute("disposalMethod", "none")
            setAttribute("userInputFlag", "FALSE")
            setAttribute("transparentColorFlag", "FALSE")
            setAttribute("delayTime", delay.toString())
            setAttribute("transparentColorIndex", "0")
        }
        val commentsNode = getNode(root, "CommentExtensions")
        commentsNode.setAttribute("CommentExtension", "Created by: https://memorynotfound.com")
        val appExtensionsNode = getNode(root, "ApplicationExtensions")
        val child = IIOMetadataNode("ApplicationExtension")
        child.setAttribute("applicationID", "NETSCAPE")
        child.setAttribute("authenticationCode", "2.0")
        val loopContinuously = if (loop) 0x0 else 0x1
        child.userObject = byteArrayOf(
            0x1,
            (loopContinuously and 0xFF).toByte(),
            ((loopContinuously shl 8) and 0xFF).toByte()
        )
        appExtensionsNode.appendChild(child)
        metadata.setFromTree(metaFormatName, root)
    }

    @Throws(IOException::class)
    fun writeToSequence(img: RenderedImage) {
        writer.writeToSequence(IIOImage(img, null, metadata), params)
    }

    @Throws(IOException::class)
    fun close() {
        writer.endWriteSequence()
    }
}

private fun getNode(rootNode: IIOMetadataNode, nodeName: String): IIOMetadataNode {
    val nNodes = rootNode.length
    for (i in 0 until nNodes) {
        if (rootNode.item(i).nodeName.equals(nodeName, ignoreCase = true)) {
            return rootNode.item(i) as IIOMetadataNode
        }
    }
    val node = IIOMetadataNode(nodeName)
    rootNode.appendChild(node)
    return node
}