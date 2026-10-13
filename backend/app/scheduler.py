"""Background refresh of the public OpenAPI data (구조동물, 분실동물).

Runs once at startup and then every SYNC_INTERVAL_MINUTES in a daemon thread,
so '오늘 구조' and recent lost reports stay current without a cron job.
"""

import logging
import threading

from app.config import settings

logger = logging.getLogger("animalloo.sync")

SYNC_DAYS = 3


def _run_once() -> None:
    # Imported lazily: the scripts pull in httpx/DB setup only when syncing.
    from scripts import sync_lost_animals, sync_rescued_animals

    for name, job in (("rescued", sync_rescued_animals.sync), ("lost", sync_lost_animals.sync)):
        try:
            logger.info("sync %s: %d items", name, job(SYNC_DAYS))
        except Exception:
            logger.exception("sync %s failed", name)


def start(stop: threading.Event) -> threading.Thread | None:
    if settings.sync_interval_minutes <= 0:
        logger.info("public data sync disabled (SYNC_INTERVAL_MINUTES=0)")
        return None
    if not settings.animal_api_key:
        logger.warning("public data sync skipped: ANIMAL_API_KEY is not set")
        return None

    def loop() -> None:
        while not stop.is_set():
            _run_once()
            stop.wait(settings.sync_interval_minutes * 60)

    thread = threading.Thread(target=loop, name="public-data-sync", daemon=True)
    thread.start()
    return thread
