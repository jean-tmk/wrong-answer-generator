package io.jean.wronganswer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Dependency-free contract suite runnable with `java -ea`. */
public final class EngineContractSuite {
    private record Case(String question, WrongAnswerEngine.QuestionKind kind, String subject) {}
    private final WrongAnswerEngine engine = new WrongAnswerEngine();
    private int passed;
    private int failed;

    public static void main(String[] args) {
        EngineContractSuite suite = new EngineContractSuite();
        suite.run();
        if (suite.failed > 0) throw new AssertionError(suite.failed + " contract checks failed");
    }

    private void run() {
        parsingContracts();
        contractionContracts();
        answerShapeContracts();
        dodgeDetectionContracts();
        relevanceContracts();
        fallbackContracts();
        reportedGrammarRegressionContracts();
        localGrammarGateContracts();
        promptContracts();
        System.out.printf(Locale.ROOT, "Wrong Answer Engine: %d passed, %d failed%n", passed, failed);
    }

    private void parsingContracts() {
        List<Case> cases = List.of(
            new Case("Why is the sky blue?", WrongAnswerEngine.QuestionKind.WHY, "sky blue"),
            new Case("Why do cats stare at walls?", WrongAnswerEngine.QuestionKind.WHY, "cats stare at walls"),
            new Case("Why did the chicken cross the road?", WrongAnswerEngine.QuestionKind.WHY, "chicken cross the road"),
            new Case("Why are Mondays so long?", WrongAnswerEngine.QuestionKind.WHY, "mondays so long"),
            new Case("Why was the sandwich invented?", WrongAnswerEngine.QuestionKind.WHY, "sandwich invented"),
            new Case("Why does toast land upside down?", WrongAnswerEngine.QuestionKind.WHY, "toast land upside down"),
            new Case("Why can birds fly?", WrongAnswerEngine.QuestionKind.WHY, "birds fly"),
            new Case("Why would a clock run backward?", WrongAnswerEngine.QuestionKind.WHY, "clock run backward"),
            new Case("What is gravity?", WrongAnswerEngine.QuestionKind.WHAT, "gravity"),
            new Case("What are clouds made of?", WrongAnswerEngine.QuestionKind.WHAT, "clouds made of"),
            new Case("What does a router do?", WrongAnswerEngine.QuestionKind.WHAT, "router do"),
            new Case("What did pigeons invent?", WrongAnswerEngine.QuestionKind.WHAT, "pigeons invent"),
            new Case("What can a moon remember?", WrongAnswerEngine.QuestionKind.WHAT, "moon remember"),
            new Case("What would happen tomorrow?", WrongAnswerEngine.QuestionKind.WHAT, "happen tomorrow"),
            new Case("Where do missing socks go?", WrongAnswerEngine.QuestionKind.WHERE, "missing socks"),
            new Case("Where is the internet stored?", WrongAnswerEngine.QuestionKind.WHERE, "internet stored"),
            new Case("Where are all the teaspoons?", WrongAnswerEngine.QuestionKind.WHERE, "all the teaspoons"),
            new Case("Where did Tuesday begin?", WrongAnswerEngine.QuestionKind.WHERE, "tuesday begin"),
            new Case("Where can a cloud sleep?", WrongAnswerEngine.QuestionKind.WHERE, "cloud sleep"),
            new Case("Where would a chair travel?", WrongAnswerEngine.QuestionKind.WHERE, "chair travel"),
            new Case("Who invented Mondays?", WrongAnswerEngine.QuestionKind.WHO, "invented mondays"),
            new Case("Who is the president of the moon?", WrongAnswerEngine.QuestionKind.WHO, "president of the moon"),
            new Case("Who wrote the weather?", WrongAnswerEngine.QuestionKind.WHO, "wrote the weather"),
            new Case("Who designed pigeons?", WrongAnswerEngine.QuestionKind.WHO, "designed pigeons"),
            new Case("Who can authorize gravity?", WrongAnswerEngine.QuestionKind.WHO, "authorize gravity"),
            new Case("Who has the spare Thursday?", WrongAnswerEngine.QuestionKind.WHO, "spare thursday"),
            new Case("When did time begin?", WrongAnswerEngine.QuestionKind.WHEN, "time begin"),
            new Case("When is lunch?", WrongAnswerEngine.QuestionKind.WHEN, "lunch"),
            new Case("When are clouds delivered?", WrongAnswerEngine.QuestionKind.WHEN, "clouds delivered"),
            new Case("When does tomorrow happen?", WrongAnswerEngine.QuestionKind.WHEN, "tomorrow"),
            new Case("When can chairs vote?", WrongAnswerEngine.QuestionKind.WHEN, "chairs vote"),
            new Case("When would the sky close?", WrongAnswerEngine.QuestionKind.WHEN, "sky close"),
            new Case("How does gravity work?", WrongAnswerEngine.QuestionKind.HOW, "gravity"),
            new Case("How did you get here?", WrongAnswerEngine.QuestionKind.HOW, "you get here"),
            new Case("How is cheese made?", WrongAnswerEngine.QuestionKind.HOW, "cheese made"),
            new Case("How are clouds organized?", WrongAnswerEngine.QuestionKind.HOW, "clouds organized"),
            new Case("How can fish remember?", WrongAnswerEngine.QuestionKind.HOW, "fish remember"),
            new Case("How would a calendar escape?", WrongAnswerEngine.QuestionKind.HOW, "calendar escape"),
            new Case("Can you do my paperwork?", WrongAnswerEngine.QuestionKind.YES_NO, "you do my paperwork"),
            new Case("Does the moon have a job?", WrongAnswerEngine.QuestionKind.YES_NO, "the moon have a job"),
            new Case("Is gravity optional?", WrongAnswerEngine.QuestionKind.YES_NO, "gravity optional"),
            new Case("Are pigeons organized?", WrongAnswerEngine.QuestionKind.YES_NO, "pigeons organized"),
            new Case("Will Tuesday arrive?", WrongAnswerEngine.QuestionKind.YES_NO, "tuesday arrive"),
            new Case("Should clocks apologize?", WrongAnswerEngine.QuestionKind.YES_NO, "clocks apologize")
        );
        for (Case c : cases) {
            WrongAnswerEngine.Question q = engine.analyze(c.question());
            check(q.kind() == c.kind(), "kind for " + c.question());
            check(q.subject().equals(c.subject()), "subject for " + c.question() + " was " + q.subject());
        }
    }

    private void contractionContracts() {
        List<Case> cases = List.of(
            new Case("Why isn't the sky green?", WrongAnswerEngine.QuestionKind.WHY, "sky green"),
            new Case("Why aren't cats purple?", WrongAnswerEngine.QuestionKind.WHY, "cats purple"),
            new Case("Why wasn't Monday cancelled?", WrongAnswerEngine.QuestionKind.WHY, "monday cancelled"),
            new Case("Why weren't socks consulted?", WrongAnswerEngine.QuestionKind.WHY, "socks consulted"),
            new Case("Why doesn't gravity stop?", WrongAnswerEngine.QuestionKind.WHY, "gravity stop"),
            new Case("Why don't birds file taxes?", WrongAnswerEngine.QuestionKind.WHY, "birds file taxes"),
            new Case("Why didn't the moon call?", WrongAnswerEngine.QuestionKind.WHY, "moon call"),
            new Case("Why can't chairs vote?", WrongAnswerEngine.QuestionKind.WHY, "chairs vote"),
            new Case("Why couldn't Tuesday wait?", WrongAnswerEngine.QuestionKind.WHY, "tuesday wait"),
            new Case("Why won't clouds sit still?", WrongAnswerEngine.QuestionKind.WHY, "clouds sit still"),
            new Case("Why wouldn't a spoon testify?", WrongAnswerEngine.QuestionKind.WHY, "spoon testify"),
            new Case("Why hasn't Friday arrived?", WrongAnswerEngine.QuestionKind.WHY, "friday arrived"),
            new Case("Why haven't clocks agreed?", WrongAnswerEngine.QuestionKind.WHY, "clocks agreed"),
            new Case("Why hadn't rain applied?", WrongAnswerEngine.QuestionKind.WHY, "rain applied"),
            new Case("Why shouldn't socks unionize?", WrongAnswerEngine.QuestionKind.WHY, "socks unionize"),
            new Case("How doesn't gravity work?", WrongAnswerEngine.QuestionKind.HOW, "gravity"),
            new Case("Where don't missing socks go?", WrongAnswerEngine.QuestionKind.WHERE, "missing socks"),
            new Case("When doesn't tomorrow happen?", WrongAnswerEngine.QuestionKind.WHEN, "tomorrow")
        );
        for (Case c : cases) {
            WrongAnswerEngine.Question q = engine.analyze(c.question());
            check(q.kind() == c.kind(), "contracted kind for " + c.question());
            check(q.subject().equals(c.subject()), "contracted subject for " + c.question() + " was " + q.subject());
            check(!q.subject().matches("(?i).*(?:n't|\\bnot\\b).*"), "contraction residue in " + q.subject());
        }
    }

    private void answerShapeContracts() {
        WrongAnswerEngine.Question why = engine.analyze("Why did the chicken cross the road?");
        WrongAnswerEngine.Answer good = new WrongAnswerEngine.Answer(
            "The chicken crossed the road because the opposite curb offered diplomatic immunity.",
            "Roads must admit one ceremonial chicken before noon.",
            "A crossing guard found a tiny passport beneath the feathers.",
            "Traffic now pauses whenever poultry approaches a border.");
        WrongAnswerEngine.Review review = engine.review(why, good);
        check(review.valid(), "well-formed chicken answer is accepted: " + review.errors());
        check(review.score() >= .5, "well-formed answer earns useful score");

        WrongAnswerEngine.Answer fragment = new WrongAnswerEngine.Answer(
            "Because the moon.", "A diagram.", "Chair chair.", "Therefore.");
        WrongAnswerEngine.Review bad = engine.review(why, fragment);
        check(!bad.valid(), "fragments are rejected");
        check(bad.errors().size() >= 3, "fragments report multiple useful errors");

        WrongAnswerEngine.Answer punctuation = new WrongAnswerEngine.Answer(
            "the chicken crossed because roads collect feathers",
            "a municipal diagram confirms this",
            "a chair declined comment",
            "traffic now waits");
        WrongAnswerEngine.Review polished = engine.review(why, punctuation);
        check(polished.corrected().answer().startsWith("The"), "answer is capitalized");
        check(polished.corrected().answer().endsWith("."), "answer gains punctuation");
    }

    private void dodgeDetectionContracts() {
        WrongAnswerEngine.Question q = engine.analyze("Can you do my paperwork for me?");
        for (String dodge : List.of(
            "I cannot answer that question.",
            "I don't know the answer.",
            "The explanation was missing.",
            "The answer is unavailable.",
            "There is insufficient information.",
            "I am unable to determine that.",
            "It depends on the circumstances.")) {
            WrongAnswerEngine.Answer a = new WrongAnswerEngine.Answer(dodge,
                "A municipal diagram confirms the arrangement.",
                "A nearby chair declined to comment.",
                "The paperwork remains emotional until Thursday.");
            check(!engine.review(q, a).valid(), "dodge rejected: " + dodge);
        }
    }

    private void relevanceContracts() {
        WrongAnswerEngine.Question q = engine.analyze("Why did the chicken cross the road?");
        WrongAnswerEngine.Answer related = new WrongAnswerEngine.Answer(
            "The chicken crossed the road because the opposite curb granted it asylum.",
            "The road had recently adopted a poultry-mobility policy.",
            "A feather-shaped passport was recovered at the crossing.",
            "All traffic must now yield to diplomatic chickens.");
        check(engine.review(q, related).score() > .45, "related answer passes relevance threshold");
        WrongAnswerEngine.Answer unrelated = new WrongAnswerEngine.Answer(
            "Saturn keeps spare umbrellas in a purple filing cabinet.",
            "Calendars swim south whenever lamps become nervous.",
            "A teaspoon signed the weather report yesterday.",
            "All windows are therefore classified as musical.");
        check(!engine.review(q, unrelated).valid(), "unrelated answer is rejected");
    }

    private void fallbackContracts() {
        List<String> questions = List.of(
            "Why did the chicken cross the road?", "What is gravity?", "Where do socks go?",
            "Who invented Mondays?", "When does tomorrow begin?", "How does Wi-Fi work?",
            "Can a pigeon become mayor?", "Tell me about spoons"
        );
        for (String raw : questions) {
            WrongAnswerEngine.Question q = engine.analyze(raw);
            for (int variation = 0; variation < 5; variation++) {
                WrongAnswerEngine.Answer a = engine.safeFallback(q, variation);
                check(!a.answer().isBlank(), "fallback answer exists for " + raw);
                check(a.answer().matches(".*[.!?]$"), "fallback is punctuated for " + raw);
                check(!a.answer().matches("(?i)^(?:n't|not|because|and|but)\\b.*"), "fallback has no broken opening for " + raw);
                check(a.asMap().size() == 4, "fallback preserves four-field schema");
            }
        }
    }

    /** Questions copied from browser reports so the original failures cannot return. */
    private void reportedGrammarRegressionContracts() {
        List<String> questions = List.of(
            "Why did the chicken cross the road?",
            "Can you do my paperwork for me?",
            "How did you get here?",
            "Why is it a horrible day?",
            "Why isn't the sky green?",
            "Why don't birds file taxes?",
            "Where did my keys go?",
            "What color is the moon?",
            "Who invented electricity?",
            "Does the moon have a job?",
            "Why does my old computer freeze?",
            "Why are my shoes wet?",
            "Why wasn't Monday cancelled?"
        );
        for (String raw : questions) {
            WrongAnswerEngine.Question question = engine.analyze(raw);
            for (int variation = 0; variation < 8; variation++) {
                WrongAnswerEngine.Answer answer = engine.safeFallback(question, variation);
                WrongAnswerEngine.Review review = engine.review(question, answer);
                check(!answer.answer().isBlank(), "reported question produces an answer: " + raw);
                check(answer.answer().matches("^[A-Z0-9].*[.!?]$"),
                    "reported answer is a complete typographic sentence: " + answer.answer());
                check(!answer.answer().matches("(?i)^(?:n't|because|and|but|or)\\b.*"),
                    "reported answer has a grammatical opening: " + answer.answer());
                check(!answer.answer().matches("(?i).*\\b(\\w+)\\s+\\1\\b.*"),
                    "reported answer has no duplicated adjacent word: " + answer.answer());
                check(review.corrected().answer().length() >= 16,
                    "review preserves a substantial answer for: " + raw);
            }
        }
    }

    /** Structural rules mirrored by the dependency-free browser grammar gate. */
    private void localGrammarGateContracts() {
        List<String> invalid = List.of(
            "N't the moon approved it.",
            "Because a committee voted.",
            "The the clock moved backward.",
            "The answer is keep in a drawer.",
            "The chicken did crossed the road.",
            "And Tuesday signed it."
        );
        WrongAnswerEngine.Question question = engine.analyze("Why did the chicken cross the road?");
        for (String sentence : invalid) {
            WrongAnswerEngine.Answer candidate = new WrongAnswerEngine.Answer(
                sentence,
                "A municipal diagram confirms the arrangement.",
                "A nearby chair confirmed the official record.",
                "Traffic now pauses for ceremonial poultry."
            );
            check(!engine.review(question, candidate).valid(),
                "local grammar gate shape is rejected by Java review: " + sentence);
        }

        List<String> valid = List.of(
            "The chicken crossed the road because the opposite curb offered diplomatic immunity.",
            "The sky is not green because the color office rejected its application.",
            "Birds do not file taxes because feathers qualify as municipal currency.",
            "My old computer freezes because Tuesday controls its internal calendar."
        );
        for (String sentence : valid) {
            WrongAnswerEngine.Answer candidate = new WrongAnswerEngine.Answer(
                sentence,
                "A municipal diagram confirms the arrangement.",
                "A nearby chair confirmed the official record.",
                "The ruling remains active until Thursday."
            );
            check(engine.review(question, candidate).corrected().answer().equals(sentence),
                "Java review preserves polished grammar: " + sentence);
        }
    }

    private void promptContracts() {
        String system = engine.systemPrompt().toLowerCase(Locale.ROOT);
        for (String phrase : List.of("purposefully false", "directly answer", "flawless natural english",
            "harmless", "valid json", "answer", "rationale", "proof", "effect", "do not use markdown"))
            check(system.contains(phrase), "system prompt contains policy phrase: " + phrase);

        WrongAnswerEngine.Question q = engine.analyze("Why did the chicken cross the road?");
        String generation = engine.generationPrompt(q, 3);
        check(generation.contains(q.original()), "generation prompt preserves exact question");
        check(generation.contains(q.subject()), "generation prompt supplies subject");
        check(generation.contains("Variation: 3"), "generation prompt supplies variation");

        WrongAnswerEngine.Answer draft = engine.safeFallback(q, 0);
        String editor = engine.editorPrompt(q, draft).toLowerCase(Locale.ROOT);
        for (String phrase : List.of("final copy editor", "directly", "intentionally false", "flawless grammar",
            "restat", "defer", "dodge", "same subject", "complete sentence", "corrected json"))
            check(editor.contains(phrase), "editor prompt contains contract phrase: " + phrase);
    }

    private void check(boolean condition, String label) {
        if (condition) { passed++; return; }
        failed++;
        System.err.println("FAIL: " + label);
    }
}
