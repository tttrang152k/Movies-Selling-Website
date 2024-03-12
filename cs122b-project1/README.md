- # Work Balancing and Jmeter Report
    
    - #### Video Demo Link: https://uci.zoom.us/rec/play/pIvExg-TT_EaVQ8CVG3NmEM8dYZkLXV_qwQyy0j9B38pcDaDXzom-hSpkbM1dYLzOBxsMJQFfKAdqTgU.eXPmbJ708q8xyvtS?canPlayFromShare=true&from=my_recording&continueMode=true&componentName=rec-play&originRequestUrl=https%3A%2F%2Fuci.zoom.us%2Frec%2Fshare%2F1l01qa_Cw-rzq7guO-SIUsqEAzBO4xUFAEgY-PBoKntDJ9mxf-g7R-xArx5Unn-u.0r9-E8nFLfnMLXYH

    - #### Instruction of deployment:
         To deploy falbix on any instance (master, slave, instance 1):
         1. Connect to tomcat manager (keep tab open to manually deploy fablix)
             - http://3.143.24.138:8080/manager/html/ (instance 1)
             - http://3.145.134.44:8080/manager/html/ (instance 2 (master))
             - http://18.219.157.91:8080/manager/html/(instance 3 (slave))
         2. (In terminal) change working directory to /s23-122b-cs122b-team-nightowls/cs122b-project1 ("cd s23-122b-cs122b-team-nightowls/cs122b-project1")
         3. run "mvn clean"
         4. run "mvn package"
         5. click "undeploy" button on tomcat manager webpage for "cs122b-spring21-project1-api-example" to remove the current falbix
         6. click "Browse..." button under "Deploy" section of tomcat manager webpage and choose the WAR file (located in s23-122b-cs122b-team-nightowls-1/cs122b-project1/target) that was created from running "mvn package" in previous step
         7. Click on hyperlink titled "cs122b-spring21-project1-api-example" under the "Path" section of the page to navigate to newly deployed Falbix


    Work Distribution:
    Thi Thuy Trang Tran
    Completed:
    Task 1: Enabled Fabflix with Connection Pooling. 
    Task 2: Setup a MySQL cluster on AWS that includes a master and a slave.
    Task 3: Setup a load balancer to balance the traffic to multiple Fabflix instances. 
    Task 4: Measured the performance of the keyword search feature using Apache JMeter.
    Testing and Inspection

- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
        
          In s23-122b-cs122b-team-nightowls-1/cs122b-project1/src/SQLQueryTemplate.java
          on lines 85 through 88 we have SQL Queries predefined using the Stored procedures we have in our mysql database.
          These Queries are used bascially all the servlets that contains Prepared Statements. A list of some typical ones are below:
              addMovieServlet.java 
              addStarServlet.java 
              MSearchServlet.java
              MBrowseServlet.java 
              FTSearchServlet.java
              etc..
  
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
        I used connection pooling in our project to reduce the cost of creating new connections to the database by keeping a "pool" of open connections that
        can be passed around from database operation to database operation. Specifically, I have prepared statements setup in our Mysql database that utilize the
        pooling, so I can send queries using the prepared statements to get a faster data return.
    
    - #### Explain how Connection Pooling works with two backend SQL.
        Connection Pooling works with the two backend SQL databases by allowing for the speedy data retrieval while also 
        dividing up the workload between the two database servers.
        
    

- # Master/Slave
  Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

       ** /etc/apache2/sites-enabled/000-default.conf (on apache server of instance 1)
       ** s23-122b-cs122b-team-nightowls-1/cs122b-project1/WebContent/META-INF/context.xml

- # How read/write requests were routed to Master/Slave SQL?
    
       ** Read requests are routed to either the Master or Slave servers based on the balancer that's set up on instance 1 (and GCP instance). 
       ** Write are routed only to the Master SQL by the load balancer.
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.


        ** log_processing.java is a script written in Java language that use to calculate the TJ and TS average times in ms. The program can handle multiple files and generate the average TJ and TS times of them.
        ** Steps:
            1. Open command prompt
            2. Run "javac log_processing.java" to compile the program
            3. Run "java log_processing file_name file_name" to calculate and output the average TS and TJ values
  


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                         |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case 1: HTTP/1 thread                          | <img width="644" alt="Single_HTTP_1" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/89e96b1e-83aa-4c85-a31d-74697ad1ba89">   | 179                        | 110.61812297734627                  | 110.38133764832794        | Expected to have the smallest average times among all cases because testing against 1 single thread. Server will respond quickly as it's not under heavy stress/load |
| Case 2: HTTP/10 threads                        | <img width="646" alt="Single_HTTP_10" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/3ab58e37-4f23-4e7a-baa9-ad3a00d194cd">   | 590                        | 523.6816009557945                   | 523.468538430904          | Expected to high increasing in the average times compared to 1 thread due to server being under heavy stress/load of the 10 threads                                  |
| Case 3: HTTPS/10 threads                       | <img width="644" alt="Single_HTTPS_10" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/96638255-5eb6-43f1-ab2e-1c685fe6ae79">   | 595                        | 510.795154185022                    | 510.26571886263514        | Expected to have similar TJ and TS average times compared to HTTP with 10 threads, higher average query time due to overhead of secure connections through HTTPS     |
| Case 4: HTTP/10 threads/No connection pooling  | <img width="646" alt="Single_HTTP_10_wo_CP" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/f9fea359-919e-428f-a156-69b7295749e8">   | 593                        | 519.7291583566015                   | 448.44834463502195        | Without connection pooling, the average times are still similar to case with connection pooling enabled                                                              |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                                                                                                         |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case 1: HTTP/1 thread                          | <img width="646" alt="Scaled_HTTP_1" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/585dcea0-e1cd-46a0-96f4-ca592c726933">   | 280                        | 208.038789025544                    | 207.2961210974456         | Expected to have similar average times as case 1 from single instance because we were sending requests to only one server with sticky session                                                                                                        |
| Case 2: HTTP/10 threads                        | <img width="647" alt="Scaled_HTTP_10" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/40862cb0-655c-4919-9367-9751c27af3b2">   | 607                        | 535.9202630497329                   | 535.1765310316482         | Expected to see a significant decrease in the average times compared to single-instance version as requested are distributed into both the master and slave instances. Loads are balance among two instances hence reduce the heavy stress onto one  |
| Case 3: HTTP/10 threads/No connection pooling  | <img width="645" alt="Scaled_HTTP_10_wo_CP" src="https://github.com/UCI-Chenli-teaching/s23-122b-cs122b-team-nightowls/assets/98499142/10d887a5-7113-4417-9193-4de15b946804">   | 548                        | 478.2522723056947                   | 476.92060842144315        | Expected to see higher average times compared to other scaled-instance cases due to the overhead of establish new connection for each request. However, comparing to single-instance version, the average times are all smaller due to load balancer |


    



    

    
