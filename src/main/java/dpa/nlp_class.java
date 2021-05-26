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
    private StanfordCoreNLP pipeline;
    private StanfordCoreNLP nerizer;


    public nlp_class() {
        Properties pipelineProps = new Properties();
        Properties nerProps = new Properties();
        pipelineProps.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        pipelineProps.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        nerProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        nerProps.setProperty("ner.applyFineGrained", "false");
        pipeline = new StanfordCoreNLP(pipelineProps);
        nerizer = new StanfordCoreNLP(nerProps);
    }

    // This is the UDF that can be called from Snowflake SQL.
    public Integer sentiment_method(String s)
    {
            Annotation annotation = pipeline.process(s);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                Integer i = RNNCoreAnnotations.getPredictedClass(tree);
                tree=null;
                annotation=null;

                return i;
            }
            return 0;

    }

    public String ner_method(String s) {
        CoreDocument doc = new CoreDocument(s);
        nerizer.annotate(doc);

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
           long startTime = System.currentTimeMillis();
           nlp_class cls = new nlp_class();
           System.out.println("Creating NLP class took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
           startTime = System.currentTimeMillis();
           System.out.println(cls.sentiment_method("After complaining about my experience at Rainbow, this store allowed me to use my $20 customer discount, as well as my $5 mail coupon. I was going 8 mile terrain running with some friends the next day and needed really good shoes. I ended up being VERY happy for the price I paid for these shoes.  The shoes were not only comfortable and stylish, but reasonably priced to begin with at a already discounted price BEFORE my discounts. Very happy with DSW THIS time."));
           System.out.println("sentiment_method took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
           startTime = System.currentTimeMillis();
           System.out.println(cls.ner_method("After complaining about my experience at Rainbow, this store allowed me to use my $20 customer discount, as well as my $5 mail coupon. I was going 8 mile terrain running with some friends the next day and needed really good shoes. I ended up being VERY happy for the price I paid for these shoes.  The shoes were not only comfortable and stylish, but reasonably priced to begin with at a already discounted price BEFORE my discounts. Very happy with DSW THIS time."));
           System.out.println("ner_method took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        }
}
