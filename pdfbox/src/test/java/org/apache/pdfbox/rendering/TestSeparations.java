/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.junit.Test;

public class TestSeparations {
    private static final String INPUT_DIR = "src/test/resources/input/rendering";
    private static final String OUTPUT_DIR = "src/test/resources/output/rendering";

    @Test
    public void test() throws IOException {
        float scale = 300 / 72f;
        String filename = "FANTASTICCMYK.ai";
        File file = new File(INPUT_DIR, filename);
        PDDocument document = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(document);        
        //renderSeparation(scale, filename, document, renderer, "PANTONE Orange 021 C");
        
        renderComposite(filename, renderer, scale);

        renderCMYKSeparations(filename, renderer, scale);

        renderSpotColorSeparations(filename, renderer, scale);

        document.close();
    }

	private void renderSeparation(float scale, String filename, PDDocument document, PDFRenderer renderer, String colorant) throws IOException {
		PDResources pageResources = document.getPage(0).getResources();

        for (COSName csName : pageResources.getColorSpaceNames()) {
            PDColorSpace colorSpace = pageResources.getColorSpace(csName);

            if (colorSpace instanceof PDSeparation) {
                PDSeparation separation = (PDSeparation)colorSpace;

                String colorantName = separation.getColorantName();

                if (colorantName.equals(colorant)) {
                    BufferedImage image = renderer.renderImage(0, scale, separation);

                    writeImage(image, OUTPUT_DIR + "/" + filename + "." + colorantName);
                }
            }
        }
    }

    private void renderSpotColorSeparations(String filename, PDFRenderer renderer, float scale) throws IOException {
        PDSeparation[] separations =  new PDSeparation[renderer.getSeparations().size()];
        
        separations = renderer.getSeparations().toArray(separations);

        for (PDSeparation separation : separations) {
            String colorant = separation.getColorantName();

            if (colorant.equals("All")) {
                continue;
            }

            BufferedImage image = renderer.renderImage(0, scale, separation);

            writeImage(image, OUTPUT_DIR + "/" + filename + "." + colorant);
        }
    }

    private void renderComposite(String filename, PDFRenderer renderer, float scale) throws IOException {
        BufferedImage image = renderer.renderImage(0, scale);

        writeImage(image, OUTPUT_DIR + "/" + filename);
    }

    private void renderCMYKSeparations(String filename, PDFRenderer renderer, float scale) throws IOException {
		String[] processColors = new String[] { "Cyan", "Magenta", "Yellow", "Black" };

        for (int i = 0; i < processColors.length; i++) {
            BufferedImage image = renderer.renderImage(0, scale, PDDeviceCMYK.INSTANCE, i);

            writeImage(image, OUTPUT_DIR + "/" + filename + "." + processColors[i]);
        }
    }

    private void writeImage(BufferedImage image, String filename) throws IOException {
        FileOutputStream outputFile = new FileOutputStream(filename + ".jpg");

        try {
            ImageIO.write(image, "jpg", outputFile);
        }
        finally {
            outputFile.close();
        }
    }
}