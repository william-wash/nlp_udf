# Java UDF Demo: Native Natural Language Processing (NLP) Using Stanford CoreNLP

### INTRODUCTION

This is a very quick Data Science/ML demo that shows the power of the Java UDF capability within Snowflake. It uses pretrained models from [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) project to do Named Entity Recognition (NER) and Sentiment natively in Snowflake. NER is a common preprocessing task within most Natural Language Processing (NLP) use-cases, and sentiment is commonly used to determine the subjective emotion of data. 

### PRE-REQUISITES

1. You will need to get access to a demo Snowflake account with Java. Please read the instructions in `#feat-java-udf` to request having Java UDFs enabled for your Snowflake account.
2. Make sure you have Docker Desktop installed: https://www.docker.com/products/docker-desktop

### SETUP

1. Clone this repo: `git clone https://github.com/william-wash/nlp_udf.git`

2. Open the terminal. Pull this docker image which includes Scala and “sbt” (simple build tool) used to build Scala apps: `docker pull hseeberger/scala-sbt:8u222_1.3.5_2.13.1`.

3. Open Docker Desktop and increase the size of the memory to 4GB so the container doesn't run out of memory when compiling/assembling the FAT jar. Hit 'Apply & Restart'.

    ![Docker settings](/images/docker_settings.png)

4. From within the parent directory of `nlp_udf`, run the docker container with a volume mapped to the current directory: `docker run -it --rm -v "$PWD":/root hseeberger/scala-sbt:8u222_1.3.5_2.13.1`

5. Download https://drive.google.com/drive/folders/1W0qwNRBe0uBhLEZAGotbCB6kbxdHGUn4?usp=sharing (you will need Snowflake credentials) and extract all the `jar` files into the `nlp_udf/lib` folder. 

6. Change into the `nlp_udf` directory and execute `sbt run` to compile and run the code. You should not see any output other than a simple print out.

7. Run `sbt assembly` to create a "fat" jar that we will upload into a Snowflake stage.

8. Type “exit” to exit out of the container. Change into “target/scala-2.12” (or similar) directory to see the newly created JAR file that is named `nlp-udf-assembly-1.0.jar`. Run “pwd” to view the path to the JAR file and copy it to the clipboard.

9. Go to the project home

10. Open the `put_command.sql` and update the `file:` path to the point to the `nlp-udf-assembly-1.0.jar` on your machine. Also update the path to your Snowflake  internal stage.

11. Now you need to run SnowSQL to upload the JAR file to a Snowflake demo account where JAVA UDFs have been enabled. Go to the project home directory using this command to upload the JAR file into an internal Snowflake stage:

    ```
    snowsql -a <your_account> -w <warehouse_name> -d <DB> -s <schema> -u <username> -f put_command.sql
    ```

12. Switch over to Snowflake UI and run the following commands to create the Java UDF and grant permissions. I use a DB called UTIL, and separate schemas for functions, procedures, and other objects. Within the func schema I created a stage called JAVA to load all of my jars used in Java UDFs

    ```sql
    create or replace function util.func.sentiment(s String)
        returns integer
        language java
        imports = ('@util.func.java/nlp-udf-assembly-1.0.jar')
        handler = 'dpa.nlp_class.sentiment_method';
    grant usage on function sentiment(string) to public;


    create or replace function util.func.ner(s String)
        returns string
        language java
        imports = ('@util.func.java/nlp-udf-assembly-1.0.jar')
        handler = 'dpa.nlp_class.ner_method';
    grant usage on function ner(string) to public;
    ``` 

13. Now you can invoke the Java UDF within a select statement like this:

    ```sql
    select ner('Dan lives in Arlington, VA and works at Amazon');
    ```

    You should now see named entities that the model was able to detect from the supplied text.

    ![model output](/images/output.png)
