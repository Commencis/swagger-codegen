package io.swagger.codegen.utils;

import java.util.Map;

/**
 * Created by olgucp on 17/05/2018.
 */
public interface TemplateToFileProcessor {

    void processTemplate(Map<String, Object> templateData, String templateName, String outputFilename);
}
