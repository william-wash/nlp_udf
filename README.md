# Java UDF Demo: Native Natural Language Processing (NLP) Using Stanford CoreNLP

### INTRODUCTION

This is a very quick Data Science/ML demo that shows the power of the Java UDF capability within Snowflake. It uses pretrained models from [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) project to do Named Entity Recognition (NER) natively in Snowflake. NER is a common preprocessing task within most Natural Language Processing (NLP) use-cases.

### PRE-REQUISITES

1. You will need to get access to a demo Snowflake account with Java. Please read the instructions in `#feat-java-udf` to request having Java UDFs enabled for your Snowflake account.
2. Make sure you have Docker Desktop installed: https://www.docker.com/products/docker-desktop

### SETUP

1. Clone this repo: `git clone https://github.com/snowflakecorp/ner_udf.git`

2. Open the terminal. Pull this docker image which includes Scala and “sbt” (simple build tool) used to build Scala apps: `docker pull hseeberger/scala-sbt:8u222_1.3.5_2.13.1`.

3. Open Docker Desktop and increase the size of the memory to 4GB so the container doesn't run out of memory when compiling/assembling the FAT jar. Hit 'Apply & Restart'.

    ![Docker settings](/images/docker_settings.png)

4. From within the parent directory of `ner_udf`, run the docker container with a volume mapped to the current directory: `docker run -it --rm -v "$PWD":/root hseeberger/scala-sbt:8u222_1.3.5_2.13.1`

5. Download https://drive.google.com/drive/folders/1W0qwNRBe0uBhLEZAGotbCB6kbxdHGUn4?usp=sharing and extract all the `jar` files into the `ner_udf/lib` folder. You would have to create a new `lib` folder.

6. Change into the `ner_udf` directory and execute `sbt run` to compile and run the code. You should not see any output other than a simple print out.

7. Run `sbt assembly` to create a "fat" jar that we will upload into a Snowflake stage.

8. Type “exit” to exit out of the container. Change into “target/scala-2.12” (or similar) directory to see the newly created JAR file that is named something like `root_2_12-0.1.0-SNAPSHOT.jar`. Rename this file to `ner-udf-assembly-1.0.jar`.
Run “pwd” to view the path to the JAR file and copy it to the clipboard.

9. Go to the project home

10. Open the `put_command.sql` and update the `file:` path to the point to the `ner-udf-assembly-1.0.jar` on your machine. Also update the  `<your_snowflake_username>` to your snowflake username to use the internal stage.

11. Now you need to run SnowSQL to upload the JAR file to a Snowflake demo account where JAVA UDFs have been enabled. Go to the project home directory using this command to upload the JAR file into an internal Snowflake stage:

    ```
    snowsql -a <your_account> -w <warehouse_name> -d <DB> -s <schema> -u <username> -f put_command.sql
    ```

12. Switch over to Snowflake UI and run the following command to create the Java UDF:

    ```sql
    create or replace function ner(s String)
        returns String
        language java
        imports = ('@~/<your_username>/ner-udf-assembly-1.0.jar')
        handler = 'dpa.ner_udf.named_entities';
    ``` 

13. Now you can invoke the Java UDF within a select statement like this:

    ```sql
    select ner('Dan lives in Arlington, VA and works at Amazon');
    ```

    You should now see named entities that the model was able to detect from the supplied text.

    ![model output](/images/output.png)
