package com.ecommerce.project.exception;

public class ResourceNotFoundException extends RuntimeException{
    String resourceName ;
    String field ;
    String fieldName ;
    Long FieldId ;

    public ResourceNotFoundException(String resourceName, String field, Long FieldId){
        super(String.format("%s not found with %s : %s", resourceName, field, FieldId));
        this.resourceName = resourceName ;
        this.field = field ;
        this.FieldId = FieldId ;
    }

    public ResourceNotFoundException(String resourceName, String field, String fieldName){
        super(String.format("%s not found with %s : %s", resourceName, field, fieldName));
        this.resourceName = resourceName ;
        this.field = field ;
        this.fieldName = fieldName ;
    }

    public ResourceNotFoundException(){

    }

}
