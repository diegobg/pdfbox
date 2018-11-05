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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.junit.Test;

public class TestSeparations {
    private static final String INPUT_DIR = "src/test/resources/input/rendering";
    private static final String OUTPUT_DIR = "src/test/resources/output/rendering";

    @Test
    public void test() throws IOException {
        String filename = "FANTASTICCMYK.ai";
        File file = new File(INPUT_DIR, filename);
        PDDocument document = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(document);
        PDResources resources = document.getPage(0).getResources();

        // Render Composite
        BufferedImage image = renderer.renderImage(0);
       
        writeImage(image, OUTPUT_DIR + "/" + filename);        

        // Render CMYK

        String[] processColors = new String[] { "Cyan", "Magenta", "Yellow", "Black" };

        for(int i = 0; i < processColors.length; i++) {
            image = renderer.renderImage(0, PDDeviceCMYK.INSTANCE, i);
       
            writeImage(image, OUTPUT_DIR + "/" + filename + "." + processColors[i] + ".pdf");
        }

        // // Render separations
        for (PDSeparation separation : renderer.getSeparations()) {
            if (separation.getColorantName().equals("All")) {
                continue;
            }

            image = renderer.renderImage(0, separation);
   
            writeImage(image, OUTPUT_DIR + "/" + filename + "." + separation.getColorantName());
        }

        document.close();
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