package placefinder.entities;

import java.util.*;

/**
 * Provides static methods for managing day trip experience categories and their hierarchy.
 */
public class DayTripExperienceCategories {

    private static final Map<String, String> MAIN_CATEGORY_DISPLAY_NAMES = new HashMap<>();
    
    private static final Map<String, List<String>> CATEGORY_HIERARCHY = new HashMap<>();

    static {
        MAIN_CATEGORY_DISPLAY_NAMES.put("landmarks_and_sightseeing", "Landmarks and Sightseeing");
        MAIN_CATEGORY_DISPLAY_NAMES.put("culture_history_and_arts", "Culture, History and Arts");
        MAIN_CATEGORY_DISPLAY_NAMES.put("nature_parks_and_outdoors", "Nature, Parks and Outdoors");
        MAIN_CATEGORY_DISPLAY_NAMES.put("family_entertainment_and_activities", "Family Entertainment and Activities");
        MAIN_CATEGORY_DISPLAY_NAMES.put("active_recreation_and_sport", "Active Recreation and Sport");
        MAIN_CATEGORY_DISPLAY_NAMES.put("relaxation_and_wellness", "Relaxation and Wellness");
        MAIN_CATEGORY_DISPLAY_NAMES.put("shopping_destinations", "Shopping Destinations");
        MAIN_CATEGORY_DISPLAY_NAMES.put("food_and_drink_experiences", "Food and Drink Experiences");
        MAIN_CATEGORY_DISPLAY_NAMES.put("nightlife_and_adult", "Nightlife and Adult");

        // Landmarks and Sightseeing
        CATEGORY_HIERARCHY.put("landmarks_and_sightseeing", Arrays.asList(
                "tourism.sights",
                "tourism.sights.castle",
                "tourism.sights.ruines",
                "tourism.sights.memorial.monument",
                "tourism.sights.memorial",
                "tourism.sights.battlefield",
                "tourism.sights.fort",
                "tourism.sights.lighthouse",
                "tourism.sights.city_gate",
                "tourism.sights.bridge",
                "tourism.sights.tower",
                "tourism.sights.archaeological_site",
                "tourism.attraction",
                "tourism.attraction.viewpoint",
                "tourism.attraction.artwork",
                "tourism.attraction.fountain",
                "tourism.attraction.clock",
                "tourism.sights.place_of_worship",
                "tourism.sights.place_of_worship.church",
                "tourism.sights.place_of_worship.cathedral",
                "tourism.sights.place_of_worship.mosque",
                "tourism.sights.place_of_worship.synagogue",
                "tourism.sights.place_of_worship.temple"
        ));

        // Culture, History and Arts
        CATEGORY_HIERARCHY.put("culture_history_and_arts", Arrays.asList(
                "entertainment.museum",
                "entertainment.culture",
                "entertainment.culture.gallery",
                "entertainment.culture.theatre",
                "entertainment.culture.arts_centre",
                "commercial.art",
                "commercial.antiques"
        ));

        // Nature, Parks and Outdoors
        CATEGORY_HIERARCHY.put("nature_parks_and_outdoors", Arrays.asList(
                "beach",
                "natural.forest",
                "natural.water",
                "natural.water.spring",
                "natural.water.hot_spring",
                "natural.mountain.peak",
                "natural.mountain.cliff",
                "natural.protected_area",
                "natural.sand.dune",
                "leisure.park",
                "leisure.park.garden",
                "leisure.park.nature_reserve",
                "leisure.picnic.picnic_site",
                "leisure.picnic.picnic_table",
                "leisure.playground"
        ));

        // Family Entertainment and Activities
        CATEGORY_HIERARCHY.put("family_entertainment_and_activities", Arrays.asList(
                "entertainment.zoo",
                "entertainment.aquarium",
                "entertainment.theme_park",
                "entertainment.water_park",
                "entertainment.escape_game",
                "entertainment.miniature_golf",
                "entertainment.bowling_alley",
                "entertainment.amusement_arcade",
                "leisure.playground"
        ));

        // Active Recreation and Sport
        CATEGORY_HIERARCHY.put("active_recreation_and_sport", Arrays.asList(
                "sport.stadium",
                "sport.swimming_pool",
                "sport.sports_centre",
                "sport.fitness.fitness_centre",
                "sport.fitness.fitness_station",
                "sport.pitch",
                "sport.track",
                "sport.ice_rink",
                "sport.horse_riding",
                "sport.dive_centre",
                "ski.lift",
                "activity.sport_club"
        ));

        // Relaxation and Wellness
        CATEGORY_HIERARCHY.put("relaxation_and_wellness", Arrays.asList(
                "leisure.spa",
                "leisure.spa.sauna",
                "beach.beach_resort",
                "natural.water.hot_spring"
        ));

        // Shopping Destinations
        CATEGORY_HIERARCHY.put("shopping_destinations", Arrays.asList(
                "commercial.shopping_mall",
                "commercial.marketplace",
                "commercial.department_store",
                "commercial.gift_and_souvenir",
                "commercial.clothing",
                "commercial.clothing.shoes",
                "commercial.clothing.accessories",
                "commercial.jewelry",
                "commercial.health_and_beauty.cosmetics",
                "commercial.hobby",
                "commercial.hobby.games",
                "commercial.hobby.music",
                "commercial.hobby.photo",
                "commercial.garden"
        ));

        // Food and Drink Experiences
        CATEGORY_HIERARCHY.put("food_and_drink_experiences", Arrays.asList(
                "catering.restaurant",
                "catering.restaurant.steak_house",
                "catering.restaurant.seafood",
                "catering.restaurant.italian",
                "catering.restaurant.chinese",
                "catering.restaurant.japanese",
                "catering.restaurant.sushi",
                "catering.restaurant.mexican",
                "catering.restaurant.indian",
                "catering.restaurant.thai",
                "catering.restaurant.vietnamese",
                "catering.restaurant.french",
                "catering.restaurant.greek",
                "catering.restaurant.spanish",
                "catering.restaurant.korean",
                "catering.restaurant.turkish",
                "catering.restaurant.barbecue",
                "catering.cafe",
                "catering.cafe.coffee_shop",
                "catering.cafe.bubble_tea",
                "catering.cafe.dessert",
                "catering.ice_cream",
                "catering.pub",
                "catering.bar",
                "catering.biergarten",
                "catering.taproom",
                "commercial.food_and_drink.drinks",
                "commercial.food_and_drink.chocolate",
                "commercial.food_and_drink.bakery"
        ));

        // Nightlife and Adult
        CATEGORY_HIERARCHY.put("nightlife_and_adult", Arrays.asList(
                "adult.nightclub",
                "adult.casino",
                "catering.bar",
                "catering.pub"
        ));
    }

    /**
     * Get all main category keys
     */
    public static List<String> getMainCategories() {
        return new ArrayList<>(MAIN_CATEGORY_DISPLAY_NAMES.keySet());
    }

    /**
     * Get display name for a category
     */
    public static String getDisplayName(String category) {
        if (category == null) {
            return "";
        }
        // If it's a main category key (snake_case), return its display name
        if (MAIN_CATEGORY_DISPLAY_NAMES.containsKey(category)) {
            return MAIN_CATEGORY_DISPLAY_NAMES.get(category);
        }
        // If it's a subcategory (contains dots), find which main category it belongs to
        if (category.contains(".")) {
            for (Map.Entry<String, List<String>> entry : CATEGORY_HIERARCHY.entrySet()) {
                if (entry.getValue().contains(category)) {
                    return MAIN_CATEGORY_DISPLAY_NAMES.get(entry.getKey());
                }
            }
        }
        // If not found, return the category as-is or formatted
        return formatCategoryName(category);
    }

    /**
     * Get all sub-categories for a given main category
     */
    public static List<String> getSubCategories(String mainCategory) {
        if (mainCategory == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(CATEGORY_HIERARCHY.getOrDefault(mainCategory, Collections.emptyList()));
    }

    /**
     * Check if a category is valid
     */
    public static boolean isValidCategory(String category) {
        if (category == null) {
            return false;
        }
        // Check if it's a main category
        if (MAIN_CATEGORY_DISPLAY_NAMES.containsKey(category)) {
            return true;
        }
        // Check if it's a valid subcategory
        if (category.contains(".")) {
            for (List<String> subCategories : CATEGORY_HIERARCHY.values()) {
                if (subCategories.contains(category)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Format a category name for display (convert snake_case to Title Case)
     */
    private static String formatCategoryName(String category) {
        if (category == null || category.isEmpty()) {
            return category;
        }
        String[] parts = category.split("[._]");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            String part = parts[i];
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.toString();
    }
}

