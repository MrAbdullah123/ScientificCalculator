package com.example.scientificcalculator
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var expressionText: TextView
    private lateinit var resultText: TextView
    private lateinit var degreeButton: Button

    private var expression = ""
    private var lastAnswer = 0.0
    private var degreeMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        expressionText = findViewById(R.id.txtExpression)
        resultText = findViewById(R.id.txtResult)
        degreeButton = findViewById(R.id.btnDeg)

        setupButtons()
    }
    // Handles clicks on calculator buttons.
    private fun setupButtons() {

        findViewById<Button>(R.id.btnZero).setOnClickListener { add("0") }
        findViewById<Button>(R.id.btnOne).setOnClickListener { add("1") }
        findViewById<Button>(R.id.btnTwo).setOnClickListener { add("2") }
        findViewById<Button>(R.id.btnThree).setOnClickListener { add("3") }
        findViewById<Button>(R.id.btnFour).setOnClickListener { add("4") }
        findViewById<Button>(R.id.btnFive).setOnClickListener { add("5") }
        findViewById<Button>(R.id.btnSix).setOnClickListener { add("6") }
        findViewById<Button>(R.id.btnSeven).setOnClickListener { add("7") }
        findViewById<Button>(R.id.btnEight).setOnClickListener { add("8") }
        findViewById<Button>(R.id.btnNine).setOnClickListener { add("9") }

        findViewById<Button>(R.id.btnDot).setOnClickListener {
            add(".")
        }

        findViewById<Button>(R.id.btnPlus).setOnClickListener {
            add("+")
        }

        findViewById<Button>(R.id.btnMinus).setOnClickListener {
            add("-")
        }

        findViewById<Button>(R.id.btnMultiply).setOnClickListener {
            add("*")
        }

        findViewById<Button>(R.id.btnDivide).setOnClickListener {
            add("/")
        }


        findViewById<Button>(R.id.btnLeft).setOnClickListener {
            add("(")
        }

        findViewById<Button>(R.id.btnRight).setOnClickListener {
            add(")")
        }


        findViewById<Button>(R.id.btnPi).setOnClickListener {
            add("pi")
        }

        findViewById<Button>(R.id.btnE).setOnClickListener {
            add("e")
        }

        findViewById<Button>(R.id.btnPower).setOnClickListener {
            add("^")
        }

        findViewById<Button>(R.id.btnSquare).setOnClickListener {
            add("^2")
        }


        findViewById<Button>(R.id.btnSqrt).setOnClickListener {
            add("sqrt(")
        }

        findViewById<Button>(R.id.btnSin).setOnClickListener {
            add("sin(")
        }

        findViewById<Button>(R.id.btnCos).setOnClickListener {
            add("cos(")
        }

        findViewById<Button>(R.id.btnTan).setOnClickListener {
            add("tan(")
        }

        findViewById<Button>(R.id.btnLog).setOnClickListener {
            add("log(")
        }

        findViewById<Button>(R.id.btnLn).setOnClickListener {
            add("ln(")
        }


        findViewById<Button>(R.id.btnAns).setOnClickListener {
            add(formatNumber(lastAnswer))
        }


        degreeButton.setOnClickListener {

            degreeMode = !degreeMode

            degreeButton.text =
                if (degreeMode) "DEG" else "RAD"

            updateResult()
        }


        findViewById<Button>(R.id.btnClear).setOnClickListener {

            expression = ""
            expressionText.text = ""
            resultText.text = "0"
        }


        findViewById<Button>(R.id.btnDelete).setOnClickListener {

            if (expression.isNotEmpty()) {

                expression = expression.dropLast(1)

                expressionText.text = expression

                updateResult()
            }
        }


        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calculate()
        }
    }

    private fun add(value: String) {

        if (resultText.text.toString() == "Error") {

            expression = ""
            resultText.text = "0"
        }

        expression += value

        expressionText.text = expression

        updateResult()
    }

    private fun updateResult() {

        if (expression.isEmpty()) {

            resultText.text = "0"
            return
        }

        try {

            val result = ExpressionParser(
                expression,
                degreeMode,
                lastAnswer
            ).parse()

            if (!result.isNaN() && !result.isInfinite()) {
                resultText.text = formatNumber(result)
            }

        } catch (_: Exception) {
            // Ignore incomplete expressions while typing.
        }
    }

    private fun calculate() {

        if (expression.isEmpty()) {
            return
        }

        try {

            val result = ExpressionParser(
                expression,
                degreeMode,
                lastAnswer
            ).parse()

            if (result.isNaN() || result.isInfinite()) {

                resultText.text = "Error"
                return
            }

            lastAnswer = result

            resultText.text = formatNumber(result)

        } catch (_: Exception) {

            resultText.text = "Error"
        }
    }

    private fun formatNumber(value: Double): String {

        if (value == 0.0) {
            return "0"
        }

        if (value.isFinite() && value % 1.0 == 0.0) {
            return value.toLong().toString()
        }

        return "%.10f".format(value)
            .trimEnd('0')
            .trimEnd('.')
    }


    private class ExpressionParser(
        private val input: String,
        private val degreeMode: Boolean,
        private val answer: Double
    ) {

        private var position = 0

        fun parse(): Double {

            val result = parseExpression()

            skipSpaces()

            if (position != input.length) {
                throw IllegalArgumentException("Unexpected character")
            }

            return result
        }

        private fun parseExpression(): Double {

            var value = parseTerm()

            while (true) {

                skipSpaces()

                if (match('+')) {

                    value += parseTerm()

                } else if (match('-')) {

                    value -= parseTerm()

                } else {

                    break
                }
            }

            return value
        }

        private fun parseTerm(): Double {

            var value = parsePower()

            while (true) {

                skipSpaces()

                if (match('*')) {

                    value *= parsePower()

                } else if (match('/')) {

                    val divisor = parsePower()

                    if (divisor == 0.0) {
                        throw ArithmeticException("Division by zero")
                    }

                    value /= divisor

                } else {

                    break
                }
            }

            return value
        }

        private fun parsePower(): Double {

            val value = parseUnary()

            skipSpaces()

            return if (match('^')) {

                val exponent = parsePower()

                value.pow(exponent)

            } else {

                value
            }
        }

        private fun parseUnary(): Double {

            skipSpaces()

            if (match('+')) {
                return parseUnary()
            }

            if (match('-')) {
                return -parseUnary()
            }

            return parsePrimary()
        }

        private fun parsePrimary(): Double {

            skipSpaces()

            if (match('(')) {

                val value = parseExpression()

                skipSpaces()

                if (!match(')')) {
                    throw IllegalArgumentException("Missing )")
                }

                return value
            }

            if (position >= input.length) {
                throw IllegalArgumentException("Unexpected end")
            }

            val current = input[position]

            if (current.isDigit() || current == '.') {
                return parseNumber()
            }

            if (current.isLetter()) {
                return parseFunctionOrConstant()
            }

            throw IllegalArgumentException("Invalid character")
        }

        private fun parseNumber(): Double {

            val start = position
            var decimalFound = false

            while (position < input.length) {

                val current = input[position]

                if (current.isDigit()) {

                    position++

                } else if (current == '.' && !decimalFound) {

                    decimalFound = true
                    position++

                } else {

                    break
                }
            }

            val numberText = input.substring(start, position)

            if (numberText == ".") {
                throw IllegalArgumentException("Invalid number")
            }

            return numberText.toDouble()
        }

        private fun parseFunctionOrConstant(): Double {

            val start = position

            while (
                position < input.length &&
                input[position].isLetter()
            ) {
                position++
            }

            val name = input
                .substring(start, position)
                .lowercase()

            return when (name) {

                "pi" -> PI

                "e" -> E

                "ans" -> answer

                "sin" -> {

                    val value = parseFunctionArgument()

                    if (degreeMode) {
                        sin(Math.toRadians(value))
                    } else {
                        sin(value)
                    }
                }

                "cos" -> {

                    val value = parseFunctionArgument()

                    if (degreeMode) {
                        cos(Math.toRadians(value))
                    } else {
                        cos(value)
                    }
                }

                "tan" -> {

                    val value = parseFunctionArgument()

                    if (degreeMode) {
                        tan(Math.toRadians(value))
                    } else {
                        tan(value)
                    }
                }

                "sqrt" -> {

                    val value = parseFunctionArgument()

                    if (value < 0) {
                        throw ArithmeticException(
                            "Square root of negative number"
                        )
                    }

                    sqrt(value)
                }

                "log" -> {

                    val value = parseFunctionArgument()

                    if (value <= 0) {
                        throw ArithmeticException(
                            "Invalid logarithm"
                        )
                    }

                    log10(value)
                }

                "ln" -> {

                    val value = parseFunctionArgument()

                    if (value <= 0) {
                        throw ArithmeticException(
                            "Invalid natural logarithm"
                        )
                    }

                    ln(value)
                }

                else -> {
                    throw IllegalArgumentException(
                        "Unknown function: $name"
                    )
                }
            }
        }

        private fun parseFunctionArgument(): Double {

            skipSpaces()

            if (!match('(')) {
                throw IllegalArgumentException("Missing (")
            }

            val value = parseExpression()

            skipSpaces()

            if (!match(')')) {
                throw IllegalArgumentException("Missing )")
            }

            return value
        }

        private fun match(character: Char): Boolean {

            skipSpaces()

            if (
                position < input.length &&
                input[position] == character
            ) {

                position++

                return true
            }

            return false
        }

        private fun skipSpaces() {

            while (
                position < input.length &&
                input[position].isWhitespace()
            ) {
                position++
            }
        }
    }
}