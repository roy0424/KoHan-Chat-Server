package com.kohan.shared.spring.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<ValidEnum, String> {
    private lateinit var enumValues: Array<out Enum<*>>

    override fun initialize(constraintAnnotation: ValidEnum) {
        enumValues = constraintAnnotation.enumClass.java.enumConstants
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean = enumValues.any { it.name == value }

    internal fun setEnumValuesForTest(values: Array<out Enum<*>>) {
        this.enumValues = values
    }
}
