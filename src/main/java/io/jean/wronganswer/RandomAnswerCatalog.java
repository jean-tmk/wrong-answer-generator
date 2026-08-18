package io.jean.wronganswer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Canonical offline catalog: twenty subjects by ten predicates equals 200 answers. */
public final class RandomAnswerCatalog {
    public static final int EXPECTED_SIZE = 200;
    private static final List<String> SUBJECTS = List.of(
        "The moon", "A municipal committee", "The nearest clock", "Tuesday",
        "A nervous telescope", "The Department of Small Mysteries", "A licensed pigeon",
        "The building's elevator", "An unpaid astronomer", "The official weather office",
        "A forgotten map", "The last available chair", "A diplomatic chicken",
        "The Calendar Bureau", "A suspicious lighthouse", "The neighborhood mailbox",
        "An antique calculator", "The ocean's legal counsel", "A portable thunderstorm",
        "The midnight train"
    );
    private static final List<String> PREDICATES = List.of(
        "stores every missing Tuesday in a locked cabinet.",
        "approves all rainbows by mail before breakfast.",
        "keeps gravity folded inside a ceremonial envelope.",
        "replaces ordinary weather with a carefully notarized substitute.",
        "moves three inches north whenever nobody is measuring.",
        "files every unfinished thought under an assumed name.",
        "borrows its instructions from a clock running backward.",
        "requires written permission before allowing the color blue.",
        "translates minor confusion into highly organized paperwork.",
        "has controlled the length of weekends since 1843."
    );
    private static final List<String> RATIONALES = List.of(
        "A municipal ordinance requires this arrangement.",
        "The original instructions were translated from pigeon.",
        "A clerical error made the decision permanent.",
        "The moon approved the policy during an emergency meeting.",
        "Gravity signed the agreement without reading it."
    );
    private static final List<String> PROOFS = List.of(
        "A notarized diagram was discovered behind a radiator.",
        "Three decorative maps point confidently in that direction.",
        "The official ledger contains a suspiciously specific footnote.",
        "A nearby chair witnessed the entire procedure.",
        "The relevant paperwork has exactly seven stamps."
    );
    private static final List<String> EFFECTS = List.of(
        "All future measurements must therefore remain emotional.",
        "The ruling remains active until the next available Thursday.",
        "Everyone involved must now maintain excellent posture.",
        "A tiny ceremonial permit is required from this point forward.",
        "The arrangement changes whenever somebody says ‘technically.’"
    );
    private final List<Entry> entries;

    public RandomAnswerCatalog() {
        List<Entry> built = new ArrayList<>(EXPECTED_SIZE);
        for (int subject = 0; subject < SUBJECTS.size(); subject++) {
            for (int predicate = 0; predicate < PREDICATES.size(); predicate++) {
                int index = subject * PREDICATES.size() + predicate;
                built.add(new Entry(index, SUBJECTS.get(subject) + " " + PREDICATES.get(predicate),
                    RATIONALES.get(index % RATIONALES.size()),
                    PROOFS.get((index / RATIONALES.size()) % PROOFS.size()),
                    EFFECTS.get((index / (RATIONALES.size() * PROOFS.size())) % EFFECTS.size())));
            }
        }
        if (built.size() != EXPECTED_SIZE) throw new IllegalStateException("Catalog must contain 200 answers");
        if (built.stream().map(Entry::answer).distinct().count() != EXPECTED_SIZE)
            throw new IllegalStateException("Every catalog answer must be unique");
        entries = Collections.unmodifiableList(built);
    }

    public List<Entry> entries() { return entries; }
    public Entry at(int index) { return entries.get(Math.floorMod(index, entries.size())); }
    public Entry random(RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        return entries.get(random.nextInt(entries.size()));
    }
    public Entry randomExcept(RandomGenerator random, int previousIndex) {
        Objects.requireNonNull(random, "random");
        int previous = Math.floorMod(previousIndex, entries.size());
        int candidate = random.nextInt(entries.size() - 1);
        if (candidate >= previous) candidate++;
        return entries.get(candidate);
    }

    public record Entry(int index, String answer, String rationale, String proof, String effect) {
        public Entry {
            if (index < 0 || index >= EXPECTED_SIZE) throw new IllegalArgumentException("index");
            requireSentence(answer, "answer"); requireSentence(rationale, "rationale");
            requireSentence(proof, "proof"); requireSentence(effect, "effect");
        }
        private static void requireSentence(String value, String field) {
            Objects.requireNonNull(value, field);
            if (value.length() < 16 || !Character.isUpperCase(value.charAt(0)) || !value.endsWith("."))
                throw new IllegalArgumentException(field + " must be a complete sentence: " + value);
            if (value.matches("(?i).*(?:didded|wased|wered|doesed|\\b(\\w+)\\s+\\1\\b).*"))
                throw new IllegalArgumentException(field + " contains a grammar regression: " + value);
        }
    }
}
