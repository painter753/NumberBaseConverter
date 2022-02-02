package converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

const val EXIT_COMMAND = "/exit"
const val BACK_COMMAND = "/back"

const val FIRST_LEVEL_MENU_MESSAGE = "Enter two numbers in format: {source base} {target base} (To quit type $EXIT_COMMAND) "

fun main() {
    while (true) {
        print(FIRST_LEVEL_MENU_MESSAGE)
        val input = readLine()!!
        when(input) {
            EXIT_COMMAND -> return
            else -> {
                val (sourceBase, targetBase) = input.split(" ").map { it.toInt() }
                if (sourceBase !in 2..36 || targetBase !in 2..36) {
                    print("ERROR. Try to exit")
                    break
                }

                while (true) {
                    println("Enter number in base $sourceBase to convert to base $targetBase (To go back type $BACK_COMMAND)")
                    val line = readLine()!!

                    when (line) {
                        BACK_COMMAND -> break
                        else -> {
                            val result = if (line.split(".").size == 1) {
                                convertIntegerPart(line, sourceBase, targetBase)
                            } else {
                                val (integerPart, fractionalPart) = line.split(".")
                                "${convertIntegerPart(integerPart, sourceBase, targetBase)}.${convertFractionalPart("0.${fractionalPart}", sourceBase, targetBase)}"
                            }
                            println("Conversion result: $result")
                        }
                    }
                }
            }
        }
    }
}

fun convertIntegerPart(integerPart: String, sourceBase: Int, targetBase: Int): String =
    when {
        sourceBase == 10 -> convertIntegerPartToTargetBase(integerPart, targetBase)
        targetBase == 10 -> convertIntegerPartToDecimal(integerPart, sourceBase)
        else -> {
            val temp = convertIntegerPartToDecimal(integerPart, sourceBase)
            convertIntegerPartToTargetBase(temp.toString(), targetBase)
        }
    }

fun convertFractionalPart(fractionalPart: String, sourceBase: Int, targetBase: Int): String =
    when {
        sourceBase == 10 -> convertFractionalPartToTargetBase(fractionalPart, targetBase)
        targetBase == 10 -> convertFractionalPartToDecimal(fractionalPart, sourceBase)
        else -> {
            val temp = convertFractionalPartToDecimal(fractionalPart, sourceBase)
            convertFractionalPartToTargetBase(temp, targetBase)
        }
    }

fun convertIntegerPartToDecimal(number: String, sourceBase: Int): String {
    var result = BigInteger.ZERO
    for (power in number.indices) {
        val num = convertSymbolToDecimal(number[number.length - 1 - power].toString(), sourceBase)
        result += Math.pow(sourceBase.toDouble(), power.toDouble()).toBigDecimal().toBigInteger() * num.toBigInteger()
    }
    return result.toString()
}
fun convertIntegerPartToTargetBase(number: String, radixInt: Int): String {
    val radix = radixInt.toBigInteger()
    var result = ""
    var quotient = BigInteger(number)
    var reminder = BigInteger.ZERO
    do {
        reminder = quotient % radix
        quotient /= radix
        result += convertDecimalToSymbol(reminder.toInt(), radix.toInt())
    } while (quotient >= radix)
    result += if (quotient.toInt() != 0) convertDecimalToSymbol(quotient.toInt(), radix.toInt()) else ""
    result = result.reversed()

    return result
}

fun convertFractionalPartToDecimal(number: String, sourceBase: Int): String {
    val (_, line) = number.split(".")
    var result = BigDecimal.ZERO
    for (power in line.indices) {
        val num = convertSymbolToDecimal(line[power].toString(), sourceBase)
        result += Math.pow(sourceBase.toDouble(), -(power.toDouble() + 1)).toBigDecimal() * num.toBigDecimal()
    }

    return result.setScale(5, RoundingMode.HALF_EVEN).toString().split(".").last()
}
fun convertFractionalPartToTargetBase(number: String, radixInt: Int): String {
    var input = BigDecimal("0.$number")
    var counter = 1
    var result = ""

    while (input != BigDecimal.ZERO && counter <= 5) {
        val (integer, fraction) = (input * radixInt.toBigDecimal()).toString().split(".")
        result += convertDecimalToSymbol(integer.toInt(), radixInt)
        input = BigDecimal("0.${fraction}")
        counter++
    }
    return result.split(".").last()
}

//formatters
//lowercase symbols 97..122
fun convertSymbolToDecimal(symbol: String, radix: Int) : Int {
    val char = symbol.lowercase().first()
    when {
        char in '0'..'9' -> return char.digitToInt()
        char in 'a'..'z' && radix > 10 && char.code - 97 < radix - 10 -> return char.code - 97 + 10
    }
    return -1
}
fun convertDecimalToSymbol(decimal: Int, radix: Int) : String {
    when {
        decimal in 0..9 -> return decimal.toString()
        decimal in 10..36 && decimal <= radix -> return Char( decimal + 87).toString()
    }
    return "#"
}
