package com.artemchep.horario.ui

import android.text.TextUtils
import com.artemchep.horario.database.models.IUser

/**
 * @author Artem Chepurnoy
 */
class Username {

    companion object {

        private val ANIMALS = arrayOf(
                "Alligator", "Anteater", "Armadillo",
                "Auroch", "Axolotl", "Badger", "Bat", "Beaver",
                "Buffalo", "Camel", "Chameleon", "Cheetah",
                "Chipmunk", "Chinchilla", "Chupacabra", "Cormorant",
                "Coyote", "Crow", "Dingo", "Dinosaur", "Dolphin",
                "Duck", "Elephant", "Ferret", "Fox", "Frog",
                "Giraffe", "Gopher", "Grizzly", "Hedgehog",
                "Hippo", "Hyena", "Jackal", "Ibex", "Ifrit",
                "Iguana", "Koala", "Kraken", "Lemur", "Leopard",
                "Liger", "Llama", "Manatee", "Mink", "Monkey",
                "Narwhal", "Nyan Cat", "Orangutan", "Otter",
                "Panda", "Penguin", "Platypus", "Python",
                "Pumpkin", "Guagga", "Rabbit", "Raccoon", "Rhino",
                "Sheep", "Shrew", "Skunk", "Slow Loris", "Squirrel",
                "Turtle", "Walrus", "Wolf", "Wolverine", "Wombat")

        fun forUser(user: IUser): String {
            return forUser(user.name, user.getKey())
        }

        fun forUser(name: String?, key: String): String {
            return if (TextUtils.isEmpty(name)) forId(key) else name as String
        }

        /**
         * Generate Google Docs like name (Anonymous Nyan Cat, etc.)
         * for given key.
         */
        fun forId(key: String): String {
            val pos = Math.abs(key.hashCode()) % ANIMALS.size
            return String.format("Anonymous %s", ANIMALS[pos])
        }

    }

}
