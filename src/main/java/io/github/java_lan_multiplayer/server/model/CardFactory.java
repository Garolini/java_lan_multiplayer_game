package io.github.java_lan_multiplayer.server.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.java_lan_multiplayer.server.model.cards.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Singleton class responsible for loading card definitions from a JSON file,
 * organizing them by group, and instantiating card objects.
 * <p>
 * The factory supports creating decks based on {@link BoardType}, generating individual
 * cards by ID, and supports multiple card types mapped to their corresponding classes.
 * <p>
 * Cards are defined in a JSON file structured as groups (e.g., "levelOne", "learningFlight"),
 * each containing an array of card definitions.
 * <p>
 * Example JSON structure:
 * {
 *   "levelOne": [
 *     { "id": 1, "cardType": "planets", ... },
 *     ...
 *   ],
 *   "learningFlight": [ ... ]
 * }
 */
public class CardFactory {

    private static CardFactory instance;
    private final Map<String, List<JsonNode>> cardsByGroup;
    private final ObjectMapper mapper;
    private final Random random;

    /**
     * Private constructor used by the singleton pattern.
     * Loads and parses the card definitions from the given JSON file.
     *
     * @param jsonFilePath The path to the JSON file containing card definitions.
     */
    private CardFactory(String jsonFilePath) {
        // Private constructor for the singleton. Loads the JSON file.
        this.mapper = new ObjectMapper();
        this.random = new Random();
        this.cardsByGroup = loadJson(jsonFilePath);

    }

    /**
     * Returns the singleton instance of {@code CardFactory}, creating it if necessary.
     * Uses the default path "public/cards.json".
     *
     * @return The singleton {@code CardFactory} instance.
     */
    public static synchronized CardFactory getInstance() {
        // Retrieves the singleton instance of CardFactory.
        if(instance == null) {
            instance = new CardFactory("public/cards.json");
        }

        return instance;
    }

    /**
     * Loads the card definitions from the specified JSON file path.
     *
     * @param path The path to the card JSON file.
     * @return A map where the key is the card group name and the value is a list of card JSON nodes.
     * @throws IllegalStateException If the file cannot be found or parsed.
     * @throws IllegalArgumentException If required fields are missing in card definitions.
     */
    private Map<String, List<JsonNode>> loadJson(String path) {
        // Loads the JSON file and builds a map of group names to a list of card jsons.
        Map<String, List<JsonNode>> groupMap = new HashMap<>();
        try {
            InputStream is = CardFactory.class.getClassLoader().getResourceAsStream(path);
            if(is == null) {
                throw new IOException("Could not find " + path);
            }
            JsonNode root = mapper.readTree(is);
            for(Iterator<String> iterator = root.fieldNames(); iterator.hasNext(); ) {
                String groupName = iterator.next();
                JsonNode cardsArray = root.get(groupName);
                if(!cardsArray.isArray()) {
                    throw new IllegalArgumentException("Group " + groupName + " must contain an array of cards.");
                }
                List<JsonNode> cardList = new ArrayList<>();
                for(JsonNode cardNode : cardsArray) {
                    if (!cardNode.has("id") || !cardNode.has("cardType")) {
                        throw new IllegalArgumentException("Card is missing required fields: " + cardNode);
                    }
                    cardList.add(cardNode);
                }
                groupMap.put(groupName, cardList);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error reading JSON file at " + path, e);
        }
        if(groupMap.containsKey("learningFlight")) {
            if(!groupMap.containsKey("levelOne")) groupMap.put("levelOne", new ArrayList<>());
            groupMap.get("levelOne").addAll(groupMap.get("learningFlight"));
        }
        return groupMap;
    }

    /**
     * Generates a card deck for a specific board type.
     * <p>
     * For {@link BoardType#LEARNING} and {@link BoardType#LEVEL_ONE}, returns 8 random cards.
     * For {@link BoardType#LEVEL_TWO}, returns 8 random level two cards plus 4 additional level one cards.
     *
     * @param deckType The type of the board to determine which group of cards to use.
     * @return A list of 8–12 instantiated {@code Card} objects depending on the board type.
     * @throws IllegalArgumentException If there aren't enough cards for the specified board type.
     */
    public List<Card> generateDeck(BoardType deckType) {
        List<Card> result = new ArrayList<>();
        List<JsonNode> cardsNodes = cardsByGroup.get(deckType.toString());

        if(cardsNodes == null || cardsNodes.size() < 8) {
            throw new IllegalArgumentException("Not enough " + deckType + "cards available. Required: 8, available: " + ((cardsNodes == null)? 0 : cardsNodes.size()));
        }
        List<JsonNode> cardsNodesCopy = new ArrayList<>(cardsNodes);
        Collections.shuffle(cardsNodesCopy, random);
        for(int i = 0; i < 8; i++) {
            result.add(createCard(cardsNodesCopy.get(i)));
        }
        if(deckType == BoardType.LEARNING || deckType == BoardType.LEVEL_ONE) return result;

        // If deck is level 2, adds 4 level 1 cards.
        List<JsonNode> level1Nodes = cardsByGroup.get("levelOne");
        if(level1Nodes == null || level1Nodes.size() < 4) {
            throw new IllegalArgumentException("Not enough level 1 cards available. Required: 4, available: " + ((level1Nodes == null)? 0 : level1Nodes.size()));
        }
        List<JsonNode> level1NodesCopy = new ArrayList<>(level1Nodes);
        Collections.shuffle(level1NodesCopy, random);
        for(int i = 0; i < 4; i++) {
            result.add(createCard(level1NodesCopy.get(i)));
        }

        return result;
    }

    /**
     * Generates a specific card by its ID for a given board type.
     * Mainly used for testing.
     *
     * @param deckType The board type group from which to search for the card.
     * @param cardId The ID of the card to be generated.
     * @return The instantiated {@code Card} object.
     * @throws IllegalArgumentException If the group doesn't exist or the card ID isn't found.
     */
    public Card generateCard(BoardType deckType, int cardId) {
        // Returns a specific card. Used for testing.
        List<JsonNode> cardGroup = cardsByGroup.get(deckType.toString());
        if (cardGroup == null) {
            throw new IllegalArgumentException("No cards found for deck type: " + deckType);
        }
        for(JsonNode cardNode : cardGroup) {
            if(cardNode.has("id") && cardNode.get("id").asInt() == cardId) {
                return createCard(cardNode);
            }
        }
        throw new IllegalArgumentException("Card with ID " + cardId + " not found.");
    }

    private static final Map<String, Class<? extends Card>> CARD_MAP = Map.ofEntries(
            Map.entry("abandonedShip", AbandonedShipCard.class),
            Map.entry("abandonedStation", AbandonedStationCard.class),
            Map.entry("combatZone", CombatZoneCard.class),
            Map.entry("epidemic", EpidemicCard.class),
            Map.entry("meteorSwarm", MeteorSwarmCard.class),
            Map.entry("openSpace", OpenSpaceCard.class),
            Map.entry("pirates", PiratesCard.class),
            Map.entry("planets", PlanetsCard.class),
            Map.entry("slavers", SlaversCard.class),
            Map.entry("smugglers", SmugglersCard.class),
            Map.entry("starDust", StarDustCard.class)
    );

    /**
     * Instantiates a {@code Card} object based on the card type defined in its JSON node.
     *
     * @param cardNode The JSON node representing the card.
     * @return A new instance of a subclass of {@code Card} mapped from the card type.
     * @throws IllegalArgumentException If the card type is not recognized.
     * @throws RuntimeException If the card data cannot be deserialized.
     */
    private Card createCard(JsonNode cardNode) {
        // Instantiates a Card from its JSON definition.

        String cardType = cardNode.get("cardType").asText();

        Class<? extends Card> cardClass = CARD_MAP.get(cardType);

        if(cardClass == null) throw new IllegalArgumentException("Unknown type in card " + cardNode.get("id") + ": " + cardType);

        try {
            return mapper.readValue(cardNode.toString(), cardClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}