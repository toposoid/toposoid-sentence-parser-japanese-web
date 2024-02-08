# toposoid-sentence-parser-japanese-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This Microservice analyzes the predicate argument structure of Japanese sentences and outputs the result in JSON.

[![Unit Test And Build Image Action](https://github.com/toposoid/toposoid-sentence-parser-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/toposoid-sentence-parser-web/actions/workflows/action.yml)

<img width="1160"  src="https://github.com/toposoid/toposoid-sentence-parser-japanese-web/assets/82787843/dc81f017-d833-4190-8a99-ab18fba6bac2">


## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

### Memory requirements For Standalone
* Required: at least 3GB of RAM
* Required: 10G or higher　of HDD

## Setup For Standalone
```bssh
docker-compose up -d
```
The first startup takes a long time until docker pull finishes.

## Usage
```bash
curl -X POST -H "Content-Type: application/json" -d '{
    "premise": [],
    "claim": [
        {
            "propositionId": "612bf3d6-bdb5-47b9-a3a6-185015c8c414",
            "sentenceId": "4a2994a1-ec7a-438b-a290-0cfb563a5170",
            "knowledge": {
                "sentence": "案ずるより産むが易し。",
                "lang": "ja_JP",
                "extentInfoJson": "{}",
                "isNegativeSentence": false
            }
        }
    ]
}' http://localhost:9001/analyze
```
Currently, isNegativeSentence is always set to false when registering data.

# Note
* This microservice uses 9001 as the default port.
* If you want to run in a remote environment or a virtual environment, change PRIVATE_IP_ADDRESS in docker-compose.yml according to your environment.
* The meaning of premise is a premise as a proposition, and it corresponds to A of A → B in a logical formula. Therefore, it is set when there is a condition for the claim. Unless there are special conditions, it is not necessary to set the premise.

## License
toposoid/toposoid-sentence-parser-japanese-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
