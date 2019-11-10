package com.airobotics.customer_api.swagger_model_poc;

public class OpenAPIHolder {

    private String openApiJson;
    private String openApiYaml;

    private static OpenAPIHolder instance;

    private OpenAPIHolder(){

    }

    public static OpenAPIHolder getInstance() {
        if (instance == null) {
            instance = new OpenAPIHolder();
        }
        return instance;
    }

    public String getOpenApiJson() {
        return openApiJson;
    }

    public void setOpenApiJson(String openApiJson) {
        this.openApiJson = openApiJson;
    }

    public String getOpenApiYaml() {
        return openApiYaml;
    }

    public void setOpenApiYaml(String openApiYaml) {
        this.openApiYaml = openApiYaml;
    }
}
