package com.artemchep.horario.interfaces

/**
 * @author Artem Chepurnoy
 */
interface Unique {

    class Utils {

        companion object {

            fun indexOf(list: List<Unique>, key: String): Int {
                val size = list.size
                return (0 until size).firstOrNull { list[it].getKey() == key } ?: -1
            }

            fun indexOf(list: List<Unique>, key: String, then: Then): Int {
                val i = indexOf(list, key)
                if (i >= 0) {
                    then.run(i)
                }
                return i
            }

        }

        @FunctionalInterface
        interface Then {

            fun run(i: Int)

        }

    }

    fun getKey(): String

}
