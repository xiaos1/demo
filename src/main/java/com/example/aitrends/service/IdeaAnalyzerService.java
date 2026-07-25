package com.example.aitrends.service;

import com.example.aitrends.model.CrawledItem;
import com.example.aitrends.model.ProgramIdea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Turns raw crawled titles/descriptions into a ranked list of well-known
 * beginner-friendly AI project archetypes. Each archetype carries a curated
 * baseline popularity score (reflecting how consistently it shows up as a
 * "first AI project" recommendation) which is boosted by how often it is
 * actually mentioned in the freshly crawled internet data.
 */
@Service
public class IdeaAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(IdeaAnalyzerService.class);

    private static final List<IdeaTemplate> CATALOG = List.of(
            new IdeaTemplate("AI Chatbot / Virtual Assistant",
                    "A conversational bot (rule-based or LLM-powered) that answers FAQs or automates a simple workflow.",
                    "Beginner", List.of("Python", "OpenAI/HuggingFace API", "Flask/FastAPI"),
                    90, List.of("chatbot", "virtual assistant", "conversational ai")),
            new IdeaTemplate("Image Classifier",
                    "A CNN or transfer-learning model that labels images (e.g. cats vs dogs, plant disease, waste sorting).",
                    "Beginner", List.of("Python", "TensorFlow/Keras", "PyTorch"),
                    85, List.of("image classif", "computer vision", "cnn")),
            new IdeaTemplate("Sentiment Analysis Tool",
                    "An NLP tool that scores text (reviews, tweets, support tickets) as positive/negative/neutral.",
                    "Beginner", List.of("Python", "NLTK/spaCy", "scikit-learn"),
                    80, List.of("sentiment analysis", "text classification", "nlp")),
            new IdeaTemplate("Spam / Fraud Detector",
                    "A classifier that flags spam email, fake reviews, or suspicious transactions.",
                    "Beginner", List.of("Python", "scikit-learn", "pandas"),
                    75, List.of("spam", "fraud detection")),
            new IdeaTemplate("Recommendation System",
                    "A collaborative-filtering or content-based engine recommending movies, products, or articles.",
                    "Intermediate", List.of("Python", "pandas", "surprise/LightFM"),
                    78, List.of("recommend", "recommender system")),
            new IdeaTemplate("Price / Demand Predictor",
                    "A regression model forecasting stock prices, house prices, or sales demand from historical data.",
                    "Intermediate", List.of("Python", "pandas", "scikit-learn/XGBoost"),
                    70, List.of("price prediction", "stock predict", "demand forecast")),
            new IdeaTemplate("Resume / Job-Matching Screener",
                    "A tool that parses resumes and scores them against a job description.",
                    "Intermediate", List.of("Python", "spaCy", "scikit-learn"),
                    60, List.of("resume screen", "job matching", "ats")),
            new IdeaTemplate("AI Content Generator",
                    "An app that generates blog posts, captions, or marketing copy using an LLM API.",
                    "Beginner", List.of("Python/Node", "OpenAI API", "LangChain"),
                    88, List.of("content generat", "text generation", "gpt", "llm app")),
            new IdeaTemplate("Object Detection App",
                    "A real-time app that detects and labels objects in images or video streams.",
                    "Intermediate", List.of("Python", "YOLO", "OpenCV"),
                    68, List.of("object detection", "yolo")),
            new IdeaTemplate("Voice Assistant / Speech-to-Text Tool",
                    "A tool that transcribes speech or executes voice commands.",
                    "Intermediate", List.of("Python", "Whisper API", "SpeechRecognition"),
                    65, List.of("speech recognition", "voice assistant", "speech-to-text")),
            new IdeaTemplate("AI Image Generator / Style Transfer",
                    "An app that generates or restyles images using diffusion models or GANs.",
                    "Intermediate", List.of("Python", "Stable Diffusion API", "PyTorch"),
                    82, List.of("style transfer", "image generation", "diffusion", "gan")),
            new IdeaTemplate("Fake News / Misinformation Detector",
                    "A classifier that flags likely misinformation in news articles or social posts.",
                    "Intermediate", List.of("Python", "scikit-learn", "NLP embeddings"),
                    55, List.of("fake news", "misinformation")),
            new IdeaTemplate("AI Study Buddy / Tutor",
                    "An app that generates quizzes, explains concepts, or summarizes study material with an LLM.",
                    "Beginner", List.of("Python/Node", "OpenAI API", "React"),
                    72, List.of("tutor", "study assistant", "quiz generat")),
            new IdeaTemplate("Plagiarism / Similarity Checker",
                    "A tool that flags duplicate or near-duplicate content using embeddings/text similarity.",
                    "Beginner", List.of("Python", "sentence-transformers", "scikit-learn"),
                    50, List.of("plagiarism", "similarity check"))
    );

    public List<ProgramIdea> rankIdeas(List<CrawledItem> crawledItems, int limit) {
        List<ProgramIdea> ranked = CATALOG.stream()
                .map(template -> score(template, crawledItems))
                .sorted((a, b) -> Integer.compare(b.getPopularityScore(), a.getPopularityScore()))
                .limit(Math.max(limit, 1))
                .collect(Collectors.toList());

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }

        printReport(ranked);
        return ranked;
    }

    private ProgramIdea score(IdeaTemplate template, List<CrawledItem> crawledItems) {
        int mentionCount = 0;
        Set<String> sources = new LinkedHashSet<>();

        for (CrawledItem item : crawledItems) {
            String haystack = (item.getTitle() + " " + item.getSnippet()).toLowerCase(Locale.ROOT);
            for (String keyword : template.keywords()) {
                if (haystack.contains(keyword)) {
                    mentionCount++;
                    sources.add(item.getSource());
                    break;
                }
            }
        }

        int popularityScore = template.baseScore() + (mentionCount * 3);
        return new ProgramIdea(template.name(), template.description(), template.difficulty(),
                template.techStack(), popularityScore, new ArrayList<>(sources));
    }

    private void printReport(List<ProgramIdea> ideas) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator())
          .append("=========== Most Popular AI Program Ideas for Beginners ===========")
          .append(System.lineSeparator());
        for (ProgramIdea idea : ideas) {
            sb.append(String.format(Locale.ROOT, "#%d  %-45s score=%-4d difficulty=%-12s seen-on=%s%n",
                    idea.getRank(), idea.getName(), idea.getPopularityScore(), idea.getDifficulty(),
                    idea.getMentionedOn().isEmpty() ? "curated-baseline" : idea.getMentionedOn()));
        }
        sb.append("=====================================================================");
        log.info(sb.toString());
    }

    private record IdeaTemplate(String name, String description, String difficulty,
                                 List<String> techStack, int baseScore, List<String> keywords) {
    }
}
