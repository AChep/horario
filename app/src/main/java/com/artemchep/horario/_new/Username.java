package com.artemchep.horario._new;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;

import com.artemchep.horario.database.models.User;

/**
 * @author Artem Chepurnoy
 */
public class Username {

    private static String[] ANIMALS = new String[]{
            "Alligator", "Anteater", "Armadillo", "Auroch",
            "Axolotl", "Badger", "Bat", "Beaver",
            "Buffalo", "Camel", "Chameleon", "Cheetah",
            "Chipmunk", "Chinchilla", "Chupacabra", "Cormorant",
            "Coyote", "Crow", "Dingo", "Dinosaur",
            "Dolphin", "Duck", "Elephant", "Ferret",
            "Fox", "Frog", "Giraffe", "Gopher",
            "Grizzly", "Hedgehog", "Hippo", "Hyena",
            "Jackal", "Ibex", "Ifrit", "Iguana",
            "Koala", "Kraken", "Lemur", "Leopard",
            "Liger", "Llama", "Manatee", "Mink",
            "Monkey", "Narwhal", "Nyan Cat", "Orangutan",
            "Otter", "Panda", "Penguin", "Platypus",
            "Python", "Pumpkin", "Guagga", "Rabbit",
            "Raccoon", "Rhino", "Sheep", "Shrew",
            "Skunk", "Slow Loris", "Squirrel", "Turtle",
            "Walrus", "Wolf", "Wolverine", "Wombat"
    };

    @NonNull
    public static String forUser(@NonNull User user) {
        return TextUtils.isEmpty(user.name) ? generateNameFor(user.key) : user.name;
    }

    /**
     * Generate Google Docs like name (Anonymous Nyan Cat, etc.)
     * for given key.
     */
    @NonNull
    private static String generateNameFor(@NonNull String key) {
        return String.format("Anonymous %s", ANIMALS[key.hashCode() % ANIMALS.length]);
    }

}
