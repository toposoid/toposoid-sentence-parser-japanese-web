# toposoid-sentence-parser-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This Microservice analyzes the predicate argument structure of Japanese sentences and outputs the result in JSON.

<img width="1206" alt="2021-09-23 16 22 55" src="https://user-images.githubusercontent.com/82787843/134468879-cffef03c-329c-45b3-b137-8686eb0b6182.png">

## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

### Memory requirements
* Required: at least 6GB of RAM
* Required: 10G or higher　of HDD

## Setup
```bssh
docker-compose up -d
```
It takes more than 20 minutes to pull the Docker image for the first time.

## Usage
```bash
curl -X POST -H "Content-Type: application/json" -d '{
    "premise":[""], 
    "claim":["案ずるより産むが易し。"]
}
' http://localhost:9001/analyze
```

# Note
* This microservice uses 9001 as the default port.
* If you want to run in a remote environment or a virtual environment, change PRIVATE_IP_ADDRESS in docker-compose.yml according to your environment.
* The meaning of premise is a premise as a proposition, and it corresponds to A of A → B in a logical formula. Therefore, it is set when there is a condition for the claim. Unless there are special conditions, it is not necessary to set the premise.

## License
toposoid/toposoid-sentence-parser-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
