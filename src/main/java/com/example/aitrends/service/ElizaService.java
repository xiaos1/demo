package com.example.aitrends.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A re-implementation of Joseph Weizenbaum's 1966 ELIZA program (the "DOCTOR" script):
 * scan the input for the highest-ranked known keyword, decompose the sentence around it
 * with a regex, and reassemble a reply from a template, reflecting pronouns (I/you, my/your...)
 * in whatever text gets carried over from the user's sentence. Falls back to generic
 * prompts - and, failing that, to something the user said earlier - when no keyword matches.
 */
@Service
public class ElizaService {

    private static final Map<String, String> REFLECTIONS = buildReflections();
    private static final List<Keyword> KEYWORDS = buildKeywords();
    private static final List<String> GREETINGS = List.of(
            "Hello. I am Eliza. How are you feeling today?",
            "Hi, I'm Eliza. What's on your mind?",
            "Good day. What would you like to talk about?"
    );
    private static final List<String> FAREWELLS = List.of(
            "Goodbye. It was nice talking to you.",
            "Thank you for talking with me. Goodbye.",
            "Goodbye. I hope our conversation was helpful."
    );
    private static final List<String> FALLBACKS = List.of(
            "Please, go on.",
            "Can you elaborate on that?",
            "I see. Tell me more.",
            "Very interesting.",
            "I'm not sure I understand you fully.",
            "What does that suggest to you?",
            "Do you feel strongly about discussing such things?",
            "That is quite interesting - please continue."
    );
    private static final Set<String> EXIT_WORDS = Set.of("bye", "goodbye", "quit", "exit");
    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9' ]");
    private static final Pattern MULTISPACE = Pattern.compile("\\s+");

    private final Map<String, Deque<String>> memories = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> fallbackCursors = new ConcurrentHashMap<>();

    public String greeting() {
        return pick(GREETINGS, 0);
    }

    public void reset(String sessionId) {
        memories.remove(sessionId);
        fallbackCursors.remove(sessionId);
    }

    public String respond(String sessionId, String input) {
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return "Please, say something.";
        }
        if (isExitPhrase(normalized)) {
            return pick(FAREWELLS, sessionId.hashCode());
        }

        for (Keyword keyword : KEYWORDS) {
            for (Decomposition decomposition : keyword.decompositions()) {
                Matcher matcher = decomposition.pattern().matcher(normalized);
                if (!matcher.matches()) {
                    continue;
                }
                if (keyword.word().equals("my")) {
                    rememberFor(sessionId, matcher);
                }
                return capitalize(fill(decomposition.nextReassembly(), matcher));
            }
        }

        Deque<String> memory = memories.get(sessionId);
        if (memory != null && !memory.isEmpty()) {
            return memory.pollFirst();
        }

        AtomicInteger cursor = fallbackCursors.computeIfAbsent(sessionId, id -> new AtomicInteger(0));
        return pick(FALLBACKS, cursor.getAndIncrement());
    }

    private static boolean isExitPhrase(String normalized) {
        for (String word : normalized.split(" ")) {
            if (!EXIT_WORDS.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private void rememberFor(String sessionId, Matcher matcher) {
        if (matcher.groupCount() < 1) {
            return;
        }
        String remainder = reflect(matcher.group(1).trim());
        if (remainder.isEmpty()) {
            return;
        }
        Deque<String> memory = memories.computeIfAbsent(sessionId, id -> new ArrayDeque<>());
        memory.addLast("Earlier you mentioned your " + remainder + ". Could you tell me more about that?");
    }

    private static String fill(String template, Matcher matcher) {
        String result = template;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String group = matcher.group(i);
            String reflected = reflect(group == null ? "" : group.trim());
            result = result.replace("{" + i + "}", reflected);
        }
        return result;
    }

    private static String reflect(String phrase) {
        if (phrase.isEmpty()) {
            return phrase;
        }
        String[] words = phrase.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(REFLECTIONS.getOrDefault(word, word));
        }
        return sb.toString();
    }

    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String lower = input.trim().toLowerCase();
        String stripped = NON_WORD.matcher(lower).replaceAll(" ");
        return MULTISPACE.matcher(stripped).replaceAll(" ").trim();
    }

    private static String capitalize(String text) {
        if (text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static String pick(List<String> options, int seed) {
        int index = Math.floorMod(seed, options.size());
        return options.get(index);
    }

    private record Keyword(String word, int rank, List<Decomposition> decompositions) {
    }

    private static final class Decomposition {
        private final Pattern pattern;
        private final List<String> reassemblies;
        private final AtomicInteger cursor = new AtomicInteger(0);

        private Decomposition(String regex, List<String> reassemblies) {
            this.pattern = Pattern.compile(regex);
            this.reassemblies = reassemblies;
        }

        private Pattern pattern() {
            return pattern;
        }

        private String nextReassembly() {
            int index = Math.floorMod(cursor.getAndIncrement(), reassemblies.size());
            return reassemblies.get(index);
        }
    }

    private static Decomposition rule(String regex, String... reassemblies) {
        return new Decomposition(regex, List.of(reassemblies));
    }

    private static Map<String, String> buildReflections() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("i", "you");
        map.put("me", "you");
        map.put("my", "your");
        map.put("mine", "yours");
        map.put("myself", "yourself");
        map.put("you", "I");
        map.put("your", "my");
        map.put("yours", "mine");
        map.put("yourself", "myself");
        map.put("am", "are");
        map.put("are", "am");
        map.put("was", "were");
        map.put("were", "was");
        map.put("i've", "you have");
        map.put("you've", "I have");
        map.put("i'll", "you will");
        map.put("you'll", "I will");
        map.put("i'm", "you are");
        map.put("you're", "I am");
        map.put("i'd", "you would");
        map.put("you'd", "I would");
        return Map.copyOf(map);
    }

    private static List<Keyword> buildKeywords() {
        List<Keyword> keywords = List.of(
                new Keyword("remember", 5, List.of(
                        rule("^i remember (.*)$",
                                "Do you often think of {1}?",
                                "Does thinking of {1} bring anything else to mind?",
                                "Why do you remember {1} just now?",
                                "What in the present situation reminds you of {1}?"),
                        rule("^do you remember (.*)$",
                                "Did you think I would forget {1}?",
                                "Why do you think I should recall {1} now?",
                                "What about {1}?"),
                        rule("^.*\\bremember.*$",
                                "Tell me more about that memory.")
                )),
                new Keyword("i want", 5, List.of(
                        rule("^i want (.*)$",
                                "What would it mean to you if you got {1}?",
                                "Why do you want {1}?",
                                "Suppose you got {1} soon?",
                                "What if you never got {1}?")
                )),
                new Keyword("i need", 5, List.of(
                        rule("^i need (.*)$",
                                "Why do you need {1}?",
                                "What would it mean to you if you got {1}?",
                                "Would it really help you to get {1}?")
                )),
                new Keyword("i am", 4, List.of(
                        rule("^i am (.*)$",
                                "Is it because you are {1} that you came to me?",
                                "How long have you been {1}?",
                                "How do you feel about being {1}?",
                                "Do you enjoy being {1}?")
                )),
                new Keyword("i'm", 4, List.of(
                        rule("^i'm (.*)$",
                                "Is it because you are {1} that you came to me?",
                                "How long have you been {1}?",
                                "Do you enjoy being {1}?")
                )),
                new Keyword("dream", 4, List.of(
                        rule("^.*\\bdream(?:s|ed|t)?\\b.*$",
                                "What does that dream suggest to you?",
                                "Do you dream often?",
                                "What persons appear in your dreams?",
                                "Do you believe that dreams have something to do with your problem?")
                )),
                new Keyword("family", 4, List.of(
                        rule("^.*\\b(mother|father|sister|brother|parents|family)\\b.*$",
                                "Tell me more about your {1}.",
                                "How do you get along with your {1}?",
                                "What was your relationship with your {1} like?",
                                "Does your family influence you much?")
                )),
                new Keyword("you are", 3, List.of(
                        rule("^you are (.*)$",
                                "What makes you think I am {1}?",
                                "Does it please you to believe I am {1}?",
                                "Perhaps you would like to be {1}.")
                )),
                new Keyword("you're", 3, List.of(
                        rule("^you're (.*)$",
                                "What makes you think I am {1}?",
                                "Does it please you to believe I am {1}?")
                )),
                new Keyword("are you", 3, List.of(
                        rule("^are you (.*)$",
                                "Why are you interested in whether I am {1} or not?",
                                "Would you prefer if I weren't {1}?",
                                "Perhaps I am {1} in your fantasies.")
                )),
                new Keyword("sorry", 3, List.of(
                        rule("^.*\\bsorry\\b.*$",
                                "Please don't apologize.",
                                "Apologies are not necessary.",
                                "It did not bother me, please continue.")
                )),
                new Keyword("if", 3, List.of(
                        rule("^if (.*)$",
                                "Do you think it's likely that {1}?",
                                "Do you wish that {1}?",
                                "Really, if {1}?")
                )),
                new Keyword("my", 2, List.of(
                        rule("^.*\\bmy (.*)$",
                                "Why do you say your {1}?",
                                "Does that suggest anything else which belongs to you?",
                                "Is it important to you that your {1}?")
                )),
                new Keyword("friend", 2, List.of(
                        rule("^.*\\bfriends?\\b.*$",
                                "Why do you bring up the topic of friends?",
                                "Do your friends worry you?",
                                "Are you sure you have any friends?")
                )),
                new Keyword("computer", 2, List.of(
                        rule("^.*\\bcomputers?\\b.*$",
                                "Do computers worry you?",
                                "Why do you mention computers?",
                                "Don't you think computers can help people?")
                )),
                new Keyword("question", 2, List.of(
                        rule("^(what|who|when|where|how)\\b.*$",
                                "Why do you ask?",
                                "Does that question interest you?",
                                "What is it you really wanted to know?",
                                "Are such questions much on your mind?")
                )),
                new Keyword("because", 1, List.of(
                        rule("^.*\\bbecause\\b.*$",
                                "Is that the real reason?",
                                "What other reasons come to mind?",
                                "Does that reason explain anything else?")
                )),
                new Keyword("perhaps", 1, List.of(
                        rule("^.*\\bperhaps\\b.*$",
                                "You do not seem quite certain.",
                                "Why the uncertain tone?",
                                "It could well be that.")
                )),
                new Keyword("always", 1, List.of(
                        rule("^.*\\balways\\b.*$",
                                "Can you think of a specific example?",
                                "When?",
                                "Really, always?")
                )),
                new Keyword("like", 1, List.of(
                        rule("^.*\\b(?:like|alike)\\b.*$",
                                "In what way?",
                                "What resemblance do you see?",
                                "Could there really be some connection?")
                )),
                new Keyword("yes", 1, List.of(
                        rule("^(?:yes|yeah|yep)\\b.*$",
                                "You seem quite positive.",
                                "Are you sure?",
                                "I understand.")
                )),
                new Keyword("no", 1, List.of(
                        rule("^no\\b.*$",
                                "Why not?",
                                "You are being a bit negative.",
                                "Are you saying no just to be negative?")
                ))
        );
        return keywords.stream()
                .sorted(Comparator.comparingInt(Keyword::rank).reversed())
                .toList();
    }
}
