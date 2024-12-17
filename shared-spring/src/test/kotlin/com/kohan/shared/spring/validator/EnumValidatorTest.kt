package com.kohan.shared.spring.validator

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class EnumValidatorTest {
    private lateinit var validator: EnumValidator
    private lateinit var constraintValidatorContext: ConstraintValidatorContext

    enum class SampleEnum {
        VALUE_ONE,
        VALUE_TWO,
        VALUE_THREE,
    }

    @BeforeEach
    fun setUp() {
        validator = EnumValidator()
        // Manually set the enum values for testing
        validator.setEnumValuesForTest(SampleEnum::class.java.enumConstants)
        constraintValidatorContext = Mockito.mock(ConstraintValidatorContext::class.java)
    }

    @Test
    fun `test valid enum value`() {
        val isValid = validator.isValid("VALUE_ONE", constraintValidatorContext)
        assertTrue(isValid, "Expected VALUE_ONE to be valid")
    }

    @Test
    fun `test invalid enum value`() {
        val isValid = validator.isValid("INVALID_VALUE", constraintValidatorContext)
        assertFalse(isValid, "Expected INVALID_VALUE to be invalid")
    }

    @Test
    fun `test null value`() {
        val isValid = validator.isValid(null, constraintValidatorContext)
        assertFalse(isValid, "Expected null to be invalid")
    }
}
