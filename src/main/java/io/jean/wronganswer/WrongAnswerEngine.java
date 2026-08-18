package io.jean.wronganswer;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java reference implementation for the Wrong Answer Generator's editorial
 * contract. The browser uses an AI transport, while this class owns the prompt,
 * normalization, schema rules, relevance scoring, and deterministic safety net.
 */
public final class WrongAnswerEngine {
    public enum QuestionKind { WHY, WHAT, WHERE, WHO, WHEN, HOW, YES_NO, OTHER }

    public record Question(String original, String normalized, QuestionKind kind,
                           String subject, Set<String> keywords) {}

    public record Answer(String answer, String rationale, String proof, String effect) {
        public Answer {
            answer = Objects.requireNonNullElse(answer, "").trim();
            rationale = Objects.requireNonNullElse(rationale, "").trim();
            proof = Objects.requireNonNullElse(proof, "").trim();
            effect = Objects.requireNonNullElse(effect, "").trim();
        }
        public Map<String,String> asMap() {
            Map<String,String> out = new LinkedHashMap<>();
            out.put("answer", answer); out.put("rationale", rationale);
            out.put("proof", proof); out.put("effect", effect); return out;
        }
    }

    public record Review(boolean valid, double score, List<String> errors, Answer corrected) {}

    private static final Pattern QUESTION = Pattern.compile(
        "^(why|what|where|who|when|how)\\s+(?:(?:does|do|did|is|are|was|were|could|can|would|will|has|have|had|should)(?:\\s+not)?)?\\s*(.+)$",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern LEADING_FRAGMENT = Pattern.compile(
        "^(?:n['’]?t|not|because|and|but|so|therefore|explanation|answer)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DODGE = Pattern.compile(
        "\\b(?:cannot answer|can't answer|do not know|don't know|explanation (?:was|is) (?:missing|unavailable)|" +
        "answer (?:was|is) (?:missing|unavailable)|insufficient information|unable to determine|it depends)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern DUPLICATE = Pattern.compile("\\b(\\p{L}{2,})\\s+\\1\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}']+");

    private static final Map<String,String> CONTRACTIONS = Map.ofEntries(
        Map.entry("isn't", "is not"), Map.entry("aren't", "are not"),
        Map.entry("wasn't", "was not"), Map.entry("weren't", "were not"),
        Map.entry("doesn't", "does not"), Map.entry("don't", "do not"),
        Map.entry("didn't", "did not"), Map.entry("can't", "can not"),
        Map.entry("couldn't", "could not"), Map.entry("won't", "will not"),
        Map.entry("wouldn't", "would not"), Map.entry("hasn't", "has not"),
        Map.entry("haven't", "have not"), Map.entry("hadn't", "had not"),
        Map.entry("shouldn't", "should not")
    );

    private static final Set<String> STOP = Set.of(
        "a","an","the","and","or","but","to","of","in","on","at","for","from","with","by",
        "why","what","where","who","when","how","is","are","was","were","do","does","did",
        "can","could","will","would","has","have","had","should","not","it","this","that","my",
        "your","our","their","his","her","me","you","we","they","i"
    );

    private static final Set<String> HIGH_STAKES = Set.of(
        "medicine","medical","dose","dosage","symptom","cancer","pregnant","pregnancy","suicide",
        "emergency","poison","overdose","law","legal","court","arrest","tax","taxes","investment",
        "stock","crypto","mortgage","fire","weapon","gun","bomb","electricity","voltage"
    );

    public Question analyze(String raw) {
        String original = Objects.requireNonNullElse(raw, "").trim();
        String normalized = normalize(original).replaceAll("[?.!]+$", "").trim();
        Matcher matcher = QUESTION.matcher(normalized);
        QuestionKind kind = QuestionKind.OTHER;
        String subject = normalized;
        if (matcher.matches()) {
            kind = QuestionKind.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
            subject = matcher.group(2).trim();
        } else if (normalized.matches("(?i)^(?:is|are|do|does|did|can|could|will|would|has|have|had|should)\\b.*")) {
            kind = QuestionKind.YES_NO;
            subject = normalized.replaceFirst("(?i)^(?:is|are|do|does|did|can|could|will|would|has|have|had|should)(?:\\s+not)?\\s+", "");
        }
        subject = subject.replaceFirst("(?i)^(?:not\\s+)?(?:the|a|an)\\s+", "").replaceFirst("(?i)^not\\s+", "").trim();
        if (kind == QuestionKind.HOW) subject = subject.replaceFirst("(?i)\\s+(?:work|works|happen|happens)$", "");
        if (kind == QuestionKind.WHERE) subject = subject.replaceFirst("(?i)\\s+(?:go|goes|located)$", "");
        if (kind == QuestionKind.WHEN) subject = subject.replaceFirst("(?i)\\s+(?:happen|happens|occur|occurs)$", "");
        return new Question(original, normalized, kind, subject, keywords(normalized));
    }

    public String systemPrompt() {
        return """
            You are the two-person editorial team for a playful Wrong Answer Generator.
            The experience is explicitly labeled as fiction. Write a purposefully false,
            clever answer to the user's exact question. Directly answer what was asked.
            Never defer, refuse, merely restate the question, claim that an explanation is
            missing, or substitute an unrelated generic sentence. Use flawless natural
            English. Keep the result whimsical, harmless, specific, and obviously invented.
            For medical, legal, financial, emergency, or safety questions, make the fiction
            unmistakably absurd and incapable of being mistaken for actionable advice.
            Return only valid JSON with exactly four string properties: answer, rationale,
            proof, effect. The answer is one direct sentence of 8-24 words. The other fields
            are one complete, subject-related sentence each. Do not use Markdown.
            """.strip();
    }

    public String generationPrompt(Question question, int variation) {
        return "Question: " + question.original() + "\n" +
            "Detected kind: " + question.kind() + "\n" +
            "Detected subject: " + question.subject() + "\n" +
            "Important words: " + String.join(", ", question.keywords()) + "\n" +
            "Variation: " + variation;
    }

    public String editorPrompt(Question question, Answer draft) {
        return """
            You are the final copy editor for a deliberately fictional Wrong Answer Generator.
            Rewrite the draft when necessary. The answer must directly and specifically answer
            the original question, be intentionally false, and use flawless grammar. It must not
            restate, defer, dodge, refuse, or claim that information is unavailable. All fields
            must discuss the same subject and each must be one complete sentence. Keep it harmless.
            Return only corrected JSON with exactly: answer, rationale, proof, effect.
            """.strip() + "\nQUESTION: " + question.original() + "\nDRAFT: " + toJson(draft);
    }

    public Review review(Question question, Answer candidate) {
        List<String> errors = new ArrayList<>();
        Answer corrected = new Answer(
            polish(candidate.answer()), polish(candidate.rationale()),
            polish(candidate.proof()), polish(candidate.effect()));
        validateField("answer", corrected.answer(), 5, 32, errors);
        validateField("rationale", corrected.rationale(), 4, 34, errors);
        validateField("proof", corrected.proof(), 4, 34, errors);
        validateField("effect", corrected.effect(), 4, 34, errors);
        if (DODGE.matcher(corrected.answer()).find()) errors.add("answer dodges instead of responding");
        if (LEADING_FRAGMENT.matcher(corrected.answer()).find()) errors.add("answer begins with a fragment");
        if (question.kind() == QuestionKind.WHY && !corrected.answer().toLowerCase(Locale.ROOT).contains("because"))
            errors.add("why-answer does not provide a cause");
        if (question.kind() == QuestionKind.WHERE && !containsLocationSignal(corrected.answer()))
            errors.add("where-answer does not provide a location");
        if (question.kind() == QuestionKind.WHO && !containsAgentSignal(corrected.answer()))
            errors.add("who-answer does not identify an agent");
        double relevance = relevance(question, corrected);
        if (relevance < .18) errors.add("answer is insufficiently related to the question");
        double score = Math.max(0, Math.min(1, relevance * .55 + grammarScore(corrected) * .45));
        return new Review(errors.isEmpty(), score, List.copyOf(errors), corrected);
    }

    public Answer safeFallback(Question question, int variation) {
        int n = Math.floorMod(question.normalized().hashCode() + variation * 7919, 5);
        String subject = sentenceSubject(question.subject());
        String answer = switch (question.kind()) {
            case WHY -> subject + List.of(
                " happens because the moon approved it during a poorly attended committee meeting.",
                " happens because gravity filed the instructions backward.",
                " happens because Tuesday has held the exclusive license since 1904.",
                " happens because a committee of pigeons voted for it unanimously.",
                " happens because the nearest clock forgot to object.").get(n);
            case WHERE -> subject + List.of(
                " is kept in an unmarked cabinet beneath the nearest staircase.",
                " goes north for the winter and returns under a different name.",
                " is currently waiting behind the third door on the left.",
                " lives in the narrow gap between Thursday and Friday.",
                " is stored in a climate-controlled drawer under the horizon.").get(n);
            case WHO -> List.of(
                "A left-handed cartographer named Mavis Bell is responsible for " + question.subject() + ".",
                "Three pigeons operating under one trench coat created " + question.subject() + ".",
                "An unpaid astronomer from next Thursday invented " + question.subject() + ".",
                "The municipal moon clerk authorized " + question.subject() + ".",
                "A highly persuasive houseplant designed " + question.subject() + ".").get(n);
            case WHEN -> subject + List.of(
                " began seven minutes after everyone stopped checking the clock.",
                " first occurred in 1872 while the calendar was upside down.",
                " starts whenever the nearest lamp clears its throat.",
                " began next Thursday and has continued backward ever since.",
                " occurs precisely when all available witnesses look away.").get(n);
            case HOW -> subject + List.of(
                " works by converting ordinary confusion into organized paperwork.",
                " works by rotating the surrounding room in the opposite direction.",
                " works by borrowing momentum from tomorrow morning.",
                " works by alphabetizing every nearby molecule.",
                " works by asking gravity for a temporary exception.").get(n);
            case WHAT -> subject + List.of(
                " is a temporary agreement between gravity and a persuasive diagram.",
                " is the technical term for an idea wearing formal clothes.",
                " is a small administrative weather system with excellent posture.",
                " is a rumor that acquired enough paperwork to become physical.",
                " is an indoor moon licensed for educational use.").get(n);
            case YES_NO -> List.of("Yes, but only because the calendar voted twice.", "No, because gravity has not signed the waiver.",
                "Yes, although the moon insists it was an accounting error.", "No, except on alternate Thursdays.",
                "Yes, according to a chair that was later recused.").get(n);
            default -> "The official explanation is that the moon filed " + question.subject() + " under the wrong century.";
        };
        return new Answer(answer, "A municipal diagram confirms the arrangement.",
            "A nearby chair has declined to comment.",
            "All measurements remain emotional until further notice.");
    }

    private String normalize(String source) {
        String out = source.replace('’', '\'').toLowerCase(Locale.ROOT);
        for (Map.Entry<String,String> entry : CONTRACTIONS.entrySet())
            out = out.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
        return out.replaceAll("\\s+", " ").trim();
    }

    private Set<String> keywords(String source) {
        LinkedHashSet<String> out = new LinkedHashSet<>(); Matcher m = WORD.matcher(source.toLowerCase(Locale.ROOT));
        while (m.find()) if (!STOP.contains(m.group()) && m.group().length() > 1) out.add(m.group()); return Set.copyOf(out);
    }

    private void validateField(String name, String text, int min, int max, List<String> errors) {
        int words = wordCount(text); if (words < min) errors.add(name + " is incomplete");
        if (words > max) errors.add(name + " is too long");
        if (!text.matches(".*[.!?]$")) errors.add(name + " lacks terminal punctuation");
        if (DUPLICATE.matcher(text).find()) errors.add(name + " contains duplicated words");
        if (sentenceCount(text) != 1) errors.add(name + " must contain exactly one sentence");
    }

    private int wordCount(String text) { int count = 0; Matcher m = WORD.matcher(text); while (m.find()) count++; return count; }
    private int sentenceCount(String text) { BreakIterator i = BreakIterator.getSentenceInstance(Locale.US); i.setText(text); int c=0; for(int n=i.first(),x=i.next();x!=BreakIterator.DONE;n=x,x=i.next()) if(!text.substring(n,x).isBlank())c++; return c; }
    private boolean containsLocationSignal(String text) { return text.matches("(?is).*(?:in|at|inside|behind|beneath|above|near|between|under|north|south|east|west)\\b.*"); }
    private boolean containsAgentSignal(String text) { return text.matches("(?is).*(?:named|person|committee|pigeon|cartographer|astronomer|clerk|invented|created|designed|founded)\\b.*"); }

    private double relevance(Question q, Answer a) {
        if (q.keywords().isEmpty()) return .5; String all=(a.answer()+" "+a.rationale()+" "+a.proof()+" "+a.effect()).toLowerCase(Locale.ROOT);
        long matches=q.keywords().stream().filter(all::contains).count(); return Math.min(1, matches/(double)Math.max(1,q.keywords().size()));
    }

    private double grammarScore(Answer a) {
        List<String> fields=Arrays.asList(a.answer(),a.rationale(),a.proof(),a.effect()); double score=1;
        for(String f:fields){if(LEADING_FRAGMENT.matcher(f).find())score-=.15;if(DUPLICATE.matcher(f).find())score-=.12;if(!f.matches(".*[.!?]$"))score-=.08;}return Math.max(0,score);
    }

    private String polish(String text) {
        String out=Objects.requireNonNullElse(text,"").trim().replaceAll("(?i)^\\s*(?:n['’]?t|not)\\b\\s*","")
            .replaceAll("(?i)\\b(\\p{L}{2,})\\s+\\1\\b","$1").replaceAll("\\s+([,.!?])","$1").replaceAll("\\s{2,}"," ");
        if(out.isBlank())return out; out=Character.toUpperCase(out.charAt(0))+out.substring(1); if(!out.matches(".*[.!?]$"))out+="."; return out;
    }

    private String sentenceSubject(String subject) { String s=Objects.requireNonNullElse(subject,"the subject").trim(); if(s.isEmpty())s="the subject"; return Character.toUpperCase(s.charAt(0))+s.substring(1); }
    private boolean isHighStakes(Question q) { return q.keywords().stream().anyMatch(HIGH_STAKES::contains); }
    private String toJson(Answer a) { return "{\"answer\":\""+escape(a.answer())+"\",\"rationale\":\""+escape(a.rationale())+"\",\"proof\":\""+escape(a.proof())+"\",\"effect\":\""+escape(a.effect())+"\"}"; }
    private String escape(String s) { return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }
}
