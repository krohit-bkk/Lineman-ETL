# Lineman-ETL
Solution to problem statement of building an ETL to read data from PostgeSQL, Spark, Hive packaged in a Dockerized form.

### Project Setup Guide
Clone this repository in your working directory (e.g. `~/docker-project/`) and follow the below steps:
1. Unzip `csv-files.zip`
   ```bash
   unzip csv-files.zip
   ```
2. Import the Maven project in InelliJ.
   Please note that this one is Java-Scala mix project and requires JDK 1.8 and Scala version 2.12.10
3. After your project setup is complete, build the project using command:
   ```bash
   mvn clean install
   ```
4. Successful build will create a `fat`/`uber` jar in `target` directory with name `SparkScalaMaven-1.0-SNAPSHOT.jar`. Copy this jar in the root directory of the docker project where you cloned this repository (e.g. `~/docker-project/`).
   In case you are facing issues while building the project in IntelliJ due to any reason, you can (alternatively) get the jar from this link:
   ```
   https://drive.google.com/drive/folders/1uLV0JBVkfWlHy1nAGhvChkoOVQ-Q0Aub?usp=sharing
   ```
   
   On successful setup,
   your project directory root would look like this:
   
   ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/e0386998-4b9e-4865-ada4-ce1a730b3e81)

5. Build the custom docker image using `Dockerfile.hive` file via the below command:
   ```bash
   docker build -t custom-hive-image -f Dockerfile.hive .
   ```
   Successful completio of this step would give the custom docker image, named `custom-hive-image:latest` as output. The same is shown below:

   ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/ac3a2720-0a2e-4864-837f-c6df5dda87ec)

   Also, the newly created image can be listed using the command:
   ```bash
   docker images
   ```
   The output of above command would be something like this:
   
   ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/6b9d001a-4f77-4fcf-b44e-dd3f0d65f826)

7. Once docker image is created successfully, create the containers using the command below:
   ```bash
   docker compose up -d --force-recreate
   ```

   The output of the above command is that it creates three docker containers, namely - `hive2`, `hive-metastore` and `postgres-metastore`.

   ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/30c51542-3018-477c-bfae-7319b4c82be5)

   The same could be also confirmed using `docker ps -a`
   
   ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/3c2c024f-ddbf-402e-ab9a-89dd8afd33ea)
   
8. As a part of final step of the docker setup, run this command to enable `crontab` on `hive2` docker container:
   ```
   docker exec -it hive2 service cron status
   ```

   The output of the above command should be:
   
   ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/9156cce0-1534-4a40-b3e6-b7bdb8c469f5)

   `crontab` is essential from automation point of view. We are scheduling to run `spark-submit` in a script `automate_with_cron.sh` scheduled on `crontab` to run every 1 minute.

### Spark-Hive Spercifications
The `docker compose` command would create three containers, namely - `hive2`, `hive-metastore` and `postgres-metastore`. More important points are listed below:
   * `hive2` is the container where we have `beeline`, `spark`.
   * `hive-metastore` is, as the name suggests, the Hive metastore. It needs PostgreSQL database to store data.
   * `postgres-metastore` is the PostgreSQL database container and it has two notable databases:
     * `metastore_db` - to manage tables/relations for managing Hive metastore
     * `lineman` - where we have data tables for this project (`lineman.order_detail` and `lineman.restaurant_detail`)
   * In case any of the container dies due to any issue, you can restart the container using `docker start <container-name>`.
   * To debug further on any container, you can use command - `docker logs <container-name>`.
   * Natually, `hive-metastore` container has a dependency on `postgres-metastore` container. This container is instantiation completes only after `postgres-metastore` is up. While booting up, this keeps polling for Postgres service to be up (dicsussed later).

Spark + Hive integration specific details are listed below:
  * To login to hive container, use the command below:
    ```
    docker exec -it hive2 bash
    ```
  * `SPARK_HOME` is located on the container `hive2` at `/opt/hive/tools/spark/`
  * Also, `hive-site.xml` has been copied to `$SPARK_HOME/conf/hive-site.xml` for integration with Hive.
  * `spark-submit` setup, below commands have been setup on `crontab` for automatic job submission:
    ```bash
    spark-submit --jars /root/postgresql-42.5.1.jar --class com.lineman.main.Ingestion /root/SparkScalaMaven-1.0-SNAPSHOT.jar >> /root/logs/$(date +"%Y%m%d%H%M%S")_ingestion.log 2>&1
    spark-submit --jars /root/postgresql-42.5.1.jar --class com.lineman.main.Transformation /root/SparkScalaMaven-1.0-SNAPSHOT.jar >> /root/logs/$(date +"%Y%m%d%H%M%S")_transformation.log 2>&1
    ```
    
    The console logs from Spark applications can be found here:
    ```
    /root/logs/
    ```

  * All other dependencies would be present by default, as provided by the docker image we have built.
  * To access Hive tables via `beeline` CLI, use the below command:
    ```
    docker exec -it hive2 beeline -u "jdbc:hive2://hive2:10000/default" -n hive -p password
    ```
  * You can use below commands to check for Hive databases and tables:
    ```sql
    USE lineman;
    SHOW TABLES;
    ```
    ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/e7068c78-6faa-4fef-8adb-f82f0ad4ceaf)

    ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/57108b43-6e86-4664-ae15-278c078e7846)

    Check the counts of tables as requested in the problem statement:
    ```sql
    SELECT COUNT(*) FROM `__order_detail_new__` UNION ALL
    SELECT COUNT(*) FROM `__restaurant_detail_new__` UNION ALL
    SELECT COUNT(*) FROM `order_detail` UNION ALL
    SELECT COUNT(*) FROM `restaurant_detail`;
    ```

    It should give the below output:
    
    ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/8bdd9645-d628-4705-93b2-e4906012a19a)
    
    Also note that the transformed tables - `lineman.__order_detail_new__` and `lineman.__restaurant_detail_new__` are configured to get `appended` (just for the sake of it) and ETL would have run multiple times already, hence, the number of records on those tables are high.
  * PostgreSQL has been configured with the credentials:
    ```
    username: hive
    password: password
    ```
    
  * Check for the source data tables in PostgreSQL as well using these commands:
    * Login to PostgreSQL's `lineman` database hosted on `postgres-metastore` container using command below:
      ```
      docker exec -it postgres-metastore psql -d lineman -Uhive
      ```
      
    * Check for tables available in database `lineman`:
      
      ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/aefa890f-fead-4101-9f6e-228dd2d74996)
   
      Below are the system tables for Hive metastore under database `metadata_db` in PostgreSQL database:
      
      ![image](https://github.com/krohit-bkk/Lineman-ETL/assets/137164694/84e69d71-80a5-4bca-93f7-f59bc4dbc9c5)
