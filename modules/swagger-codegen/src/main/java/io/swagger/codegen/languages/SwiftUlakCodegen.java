package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.utils.TemplateToFileProcessor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by olgucp on 15/05/2018.
 * Use command: swagger-codegen generate -i x.yaml -l swiftUlak
 */
public class SwiftUlakCodegen extends Swift4Codegen {

    interface OperationProcessor {
        void processEach(CodegenOperation op);
    }

    class BaseOperationProcessor {
        Map<String, String> templateFiles;
        TemplateToFileProcessor processor;

        BaseOperationProcessor(Map<String, String> templateFiles, TemplateToFileProcessor processor) {
            this.templateFiles = templateFiles;
            this.processor = processor;
        }
    }

    class RequestOperationProcessor extends BaseOperationProcessor implements OperationProcessor {

        RequestOperationProcessor(Map<String, String> templateFiles, TemplateToFileProcessor processor) {
            super(templateFiles, processor);
        }

        @Override
        public void processEach(CodegenOperation op) {
            op.path = ulakPath(op.path);
            op.httpMethod = ulakHTTPMethod(op.httpMethod);

            for (String templateName : templateFiles.keySet()) {
                String filename = requestFilename(templateName, op.operationIdCamelCase);
                if (!shouldOverwrite(filename) && new File(filename).exists()) {
                    LOGGER.info("Skipped overwriting " + filename);
                    continue;
                }
                Map<String, Object> templateData = new HashMap<String, Object>();
                templateData.put("operation", op);
                processor.processTemplate(templateData, templateName, filename);
            }
        }

        private String ulakPath(String path) {
            return path.replace("{", "\\(").replace("}", ")");
        }

        private String ulakHTTPMethod(String httpMethod) {
            return "." + httpMethod.toLowerCase();
        }
    }

    @Override
    public String getName() {
        return "swiftUlak";
    }

    protected Map<String, String> requestTemplateFiles = new HashMap<String, String>();

    public SwiftUlakCodegen() {
        super();
        typeMapping.put("Currency", "String");
        typeMapping.put("LocalDateTime", "Date");
        outputFolder = "generated-code" + File.separator + "swift";
        modelTemplateFiles.put("model.mustache", ".swift");
        requestTemplateFiles.put("request.mustache", ".swift");
        apiTemplateFiles.clear();
        embeddedTemplateDir = templateDir = "swiftUlak";
        modelPackage = File.separator + "Models";
        apiPackage = modelPackage + File.separator + "Requests";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        supportingFiles.clear();
    }

    @Override
    public void generateCustomApis(Map<String, Object> operation, TemplateToFileProcessor processor) {
        processEachOperation(operation, new RequestOperationProcessor(requestTemplateFiles, processor));
    }

    private String requestFilename(String templateName, String operationName) {
        String suffix = requestTemplateFiles.get(templateName);
        return apiFileFolder() + File.separator + operationName + "Request" + suffix;
    }

    private void processEachOperation(Map<String, Object> operation, OperationProcessor operationProcessor) {
        Map ops = (Map) operation.get("operations");
        List<CodegenOperation> opList = (List<CodegenOperation>) ops.get("operation");

        for (CodegenOperation op: opList) {
            operationProcessor.processEach(op);
        }
    }

}