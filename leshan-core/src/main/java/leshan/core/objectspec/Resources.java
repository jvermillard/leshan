/*
 * Copyright (c) 2013, Sierra Wireless
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
package leshan.core.objectspec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import leshan.core.objectspec.json.ObjectSpecDeserializer;
import leshan.core.objectspec.json.ResourceSpecDeserializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The resource descriptions for registered LWM2M objects (only OMA objects for now).
 */
public class Resources {

    private static final Logger LOG = LoggerFactory.getLogger(Resources.class);

    private static final Map<Integer, ObjectSpec> OBJECTS = new HashMap<>(); // objects by ID

    /**
     * Initializes the list of LWM2M object definitions.
     */
    public static void load() {

        synchronized (OBJECTS) {
            if (OBJECTS.isEmpty()) {

                // load OMA objects definitions from json files
                InputStream input = Resources.class.getResourceAsStream("/objectspec.json");
                if (input == null) {
                    return;
                }

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(ObjectSpec.class, new ObjectSpecDeserializer());
                gsonBuilder.registerTypeAdapter(ResourceSpec.class, new ResourceSpecDeserializer());
                Gson gson = gsonBuilder.create();

                try (Reader reader = new InputStreamReader(input)) {
                    ObjectSpec[] objectSpecs = gson.fromJson(reader, ObjectSpec[].class);
                    for (ObjectSpec objectSpec : objectSpecs) {
                        OBJECTS.put(objectSpec.id, objectSpec);
                    }
                } catch (IOException e) {
                    LOG.error("Unable to load object specification", e);
                }

                // load custom resources
                // get folder path
                String modelsFolderEnvVar = System.getenv("MODELS_FOLDER");
                String modelsFolderPath = modelsFolderEnvVar != null ? modelsFolderEnvVar : "./models";

                // check if the folder is usable
                File modelsFolder = new File(modelsFolderPath);
                if (!modelsFolder.isDirectory() || !modelsFolder.canRead()) {
                    // log only if env var is configured
                    if (modelsFolderEnvVar != null)
                        LOG.error(MessageFormat.format(
                                "Models folder {0} is not a directory or you have not the right to list its content.",
                                modelsFolderPath));
                    return;
                }

                // get all files
                for (File file : modelsFolder.listFiles()) {
                    if (!file.canRead())
                        continue;

                    if (file.getName().endsWith(".xml")) {
                        // load DDF file
                        DDFFileParser ddfFileParser = new DDFFileParser();
                        ObjectSpec objectSpec = ddfFileParser.parse(file);
                        if (objectSpec != null) {
                            if (OBJECTS.containsKey(objectSpec.id))
                                LOG.warn(MessageFormat.format("There are multiple definitions for the object {0}",
                                        objectSpec.id));
                            OBJECTS.put(objectSpec.id, objectSpec);
                        }
                    } else if (file.getName().endsWith(".json")) {
                        // load object spec json file
                        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                            ObjectSpec[] objectSpecs = gson.fromJson(reader, ObjectSpec[].class);
                            for (ObjectSpec objectSpec : objectSpecs) {
                                if (OBJECTS.containsKey(objectSpec.id))
                                    LOG.warn(MessageFormat.format("There are multiple definitions for the object {0}",
                                            objectSpec.id));
                                OBJECTS.put(objectSpec.id, objectSpec);
                            }
                        } catch (IOException e) {
                            LOG.warn(
                                    MessageFormat.format("Unable to load object specification for {0}",
                                            file.getAbsolutePath()), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the description of a given resource.
     *
     * @param objectId the object identifier
     * @param resourceId the resource identifier
     * @return the resource specification or <code>null</code> if not found
     */
    public static ResourceSpec getResourceSpec(int objectId, int resourceId) {
        ObjectSpec object = OBJECTS.get(objectId);
        if (object != null) {
            return object.resources.get(resourceId);
        }
        return null;
    }

    /**
     * Returns the description of a given object.
     *
     * @param objectId the object identifier
     * @return the object specification or <code>null</code> if not found
     */
    public static ObjectSpec getObjectSpec(int objectId) {
        return OBJECTS.get(objectId);
    }

    /**
     * @return all the objects descriptions known.
     */
    public static Collection<ObjectSpec> getObjectSpecs() {
        return Collections.unmodifiableCollection(OBJECTS.values());
    }
}
