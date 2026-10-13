"""Upload the symptom/disease knowledge graph to the Fuseki dataset.

    python -m scripts.load_fuseki --turtle-dir C:/Users/jwon/Downloads/turtle

Fuseki must already be running with an updatable /animalloo dataset, e.g.
    fuseki-server --update --tdb2 --loc=<backend>/fuseki-data /animalloo
Uploading appends; to reload from scratch, stop Fuseki and delete fuseki-data.
"""

import argparse
from pathlib import Path

import httpx

from app.config import settings

FILES = ("13_symptoms.ttl", "15_diseases.ttl", "16_reasoning_chain.ttl")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--turtle-dir", type=Path, required=True)
    args = parser.parse_args()

    data_url = settings.fuseki_query_url.rsplit("/", 1)[0] + "/data"
    for name in FILES:
        response = httpx.post(
            data_url,
            content=(args.turtle_dir / name).read_bytes(),
            headers={"Content-Type": "text/turtle; charset=utf-8"},
            timeout=60,
        )
        response.raise_for_status()
        print(f"{name}: {response.status_code}")


if __name__ == "__main__":
    main()
