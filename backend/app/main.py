import logging
import threading
from contextlib import asynccontextmanager

import httpx
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from sqlalchemy import text

from app import scheduler
from app.config import settings
from app.db import engine
from app.routers import alerts, animals, auth, diagnosis, facilities, favorites, home, hospitals, lost_reports

logging.getLogger("animalloo").setLevel(logging.INFO)
logging.getLogger("animalloo").addHandler(logging.StreamHandler())


@asynccontextmanager
async def lifespan(_: FastAPI):
    stop = threading.Event()
    scheduler.start(stop)
    yield
    stop.set()


app = FastAPI(title="AnimalLoo API", version="0.1.0", lifespan=lifespan)

for router in (auth, home, alerts, facilities, hospitals, diagnosis, animals, lost_reports, favorites):
    app.include_router(router.router)

settings.upload_path.mkdir(parents=True, exist_ok=True)
app.mount("/uploads", StaticFiles(directory=settings.upload_path), name="uploads")


@app.get("/health", tags=["시스템"])
def health():
    try:
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        database = "ok"
    except Exception as exc:  # report, don't crash the health check
        database = f"error: {exc.__class__.__name__}"
    try:
        httpx.post(settings.fuseki_query_url, data={"query": "ASK {}"}, timeout=3).raise_for_status()
        fuseki = "ok"
    except httpx.HTTPError as exc:
        fuseki = f"error: {exc.__class__.__name__}"
    return {"database": database, "fuseki": fuseki}
