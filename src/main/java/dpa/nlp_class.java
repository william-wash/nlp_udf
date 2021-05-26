package dpa;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import org.json.*;
import java.util.*;
import java.util.stream.Collectors;
import edu.stanford.nlp.trees.Tree;

public class nlp_class {

    // Create a class-level variable to store one instance of this class
    // that can be shared after initialization completes.
    private static volatile nlp_class nlp_instance = null;
    // These counters will show that the constructor was called only once
    // even though nlp_method() was called more than once.
    private static int initialization_counter = 0;
    private static int method_call_counter = 0;

    private static StanfordCoreNLP pipeline;
    private static StanfordCoreNLP nerizer;


    // This constructor would contain your expensive or thread-sensitive
    // initialization code that should be run only once.
    private nlp_class() {
        // Expensive or thread-sensitive initialization code.
        // ...

		Properties pipelineProps = new Properties();
        Properties nerProps = new Properties();
        pipelineProps.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        pipelineProps.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        nerProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        nerProps.setProperty("ner.applyFineGrained", "false");
        pipeline = new StanfordCoreNLP(pipelineProps);
        nerizer = new StanfordCoreNLP(nerProps);

        // To help prove that we initialize only once.
        ++initialization_counter;
    }

    // This uses synchronization to initialize only when we haven't
    // already finished initializing (but another thread might have
    // started initializing).
    // We use "double locking" so that after the first initialization
    // call finishes, we avoid further overhead for synchronizing.
    private static synchronized nlp_class getInstanceUsingDoubleLocking() {
        // If no one has finished initializing ...
        if (nlp_instance == null)  {
            // If no one has even started initializing ...
            synchronized (nlp_class.class) {
                if (nlp_instance == null)  {
                    nlp_instance = new nlp_class();
                }
            }
        }
        return nlp_instance;
    }

    // This is the UDF that can be called from Snowflake SQL.
    public static Integer sentiment_method(String s)
    {
        // Initialize if and only if we haven't already.
        if (nlp_instance == null)
        {
            nlp_instance = getInstanceUsingDoubleLocking();
        }
            Annotation annotation = nlp_instance.pipeline.process(s);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                Integer i = RNNCoreAnnotations.getPredictedClass(tree);
                tree=null;
                annotation=null;

                return i;
            }
            return 0;

    }

    public static String ner_method(String s) {
                // Initialize if and only if we haven't already.
        if (nlp_instance == null)
        {
            nlp_instance = getInstanceUsingDoubleLocking();
        }
        CoreDocument doc = new CoreDocument(s);
        nlp_instance.nerizer.annotate(doc);

        String nerstring = "";
        int i = 0;
        JSONArray array = new JSONArray();


        for (CoreEntityMention em : doc.entityMentions()) {
            JSONObject e = new JSONObject();
                    e.put("index", i);
                    e.put("word", em.text());
                    e.put("entity", em.entityType());
                    array.put(e);
            i += 1;
        }
        JSONObject obj = new JSONObject();
        obj.put("entities", array);
        //System.out.print(obj); 
        nerstring = obj.toString();
        doc=null;
        array=null;  

        return nerstring;
    }
    
    public static void main(String[] args) { 
           System.out.println("You should only see this line when you run the UDF locally."); 
           System.out.println(nlp_class.sentiment_method("After complaining about my experience at Rainbow, this store allowed me to use my $20 customer discount, as well as my $5 mail coupon. I was going 8 mile terrain running with some friends the next day and needed really good shoes. I ended up being VERY happy for the price I paid for these shoes.  The shoes were not only comfortable and stylish, but reasonably priced to begin with at a already discounted price BEFORE my discounts. Very happy with DSW THIS time."));
           System.out.println(nlp_class.ner_method("After complaining about my experience at Rainbow, this store allowed me to use my $20 customer discount, as well as my $5 mail coupon. I was going 8 mile terrain running with some friends the next day and needed really good shoes. I ended up being VERY happy for the price I paid for these shoes.  The shoes were not only comfortable and stylish, but reasonably priced to begin with at a already discounted price BEFORE my discounts. Very happy with DSW THIS time."));

        }
}