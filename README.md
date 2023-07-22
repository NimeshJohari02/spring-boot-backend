# spring-boot-backend

## Instructions To Run 

```bash
git clone <repo url>
cd spring-boot-backend
gradle build
gradle bootRun
```

### Features 

1. User Modelling Done using MongoDb . The Schema is rather simplistic and contains email , profile photograph and a name . 
2. Redis integrated to cache the user details and reduce the calls to Mongo
3. Image upload features added along with a maximum file size of 5 mb and constraints on format of the image .
4. Image upload links to an S3 Bucket .

