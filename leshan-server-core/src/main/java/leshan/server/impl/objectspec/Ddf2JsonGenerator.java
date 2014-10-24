/*
 * Copyright (c) 2014, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.impl.objectspec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import leshan.server.impl.objectspec.json.ObjectSpecSerializer;
import leshan.server.impl.objectspec.json.ResourceSpecSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Ddf2JsonGenerator {

    private Gson gson;

    public Ddf2JsonGenerator() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ObjectSpec.class, new ObjectSpecSerializer());
        gsonBuilder.registerTypeAdapter(ResourceSpec.class, new ResourceSpecSerializer());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    private void generate(Collection<ObjectSpec> objectSpecs, OutputStream output) throws IOException {
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(output)) {
            gson.toJson(objectSpecs, outputStreamWriter);
        }
    }

    private void generate(File input, OutputStream output) throws IOException {
        // check input exists
        if (!input.exists())
            throw new FileNotFoundException(input.toString());

        // get input files.
        File[] files;
        if (input.isDirectory()) {
            files = input.listFiles();
        } else {
            files = new File[] { input };
        }

        // parse DDF file
        List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
        DDFFileParser ddfParser = new DDFFileParser();
        for (File f : files) {
            if (f.canRead()) {
                ObjectSpec objectSpec = ddfParser.parse(f);
                if (objectSpec != null) {
                    objectSpecs.add(objectSpec);
                }
            }
        }

        // sort object by id
        Collections.sort(objectSpecs, new Comparator<ObjectSpec>() {
            @Override
            public int compare(ObjectSpec o1, ObjectSpec o2) {
                return o1.id - o2.id;
            }
        });

        // generate json
        generate(objectSpecs, output);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // default value
        String DDFFilesPath = "ddffiles";
        String outputPath = "src/main/resources/objectspec.json";

        // use arguments if they exit
        if (args.length >= 1)
            DDFFilesPath = args[1]; // the path to a DDF file or a folder which contains DDF files.
        if (args.length >= 2)
            outputPath = args[2]; // the path of the output file.

        // generate object spec file
        Ddf2JsonGenerator ddfJsonGenerator = new Ddf2JsonGenerator();
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputPath)) {
            ddfJsonGenerator.generate(new File(DDFFilesPath), fileOutputStream);
        }
    }
}
