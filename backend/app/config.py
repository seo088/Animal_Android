from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

BACKEND_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=BACKEND_DIR / ".env", extra="ignore")

    database_url: str = "postgresql+psycopg://animalloo:animalloo@localhost:5432/animalloo"
    fuseki_query_url: str = "http://localhost:3030/animalloo/query"
    jwt_secret: str = "change-me"
    jwt_expire_hours: int = 168
    upload_dir: str = "uploads"
    public_base_url: str = "http://10.0.2.2:8000"
    animal_api_key: str = ""
    sync_interval_minutes: int = 60

    @property
    def upload_path(self) -> Path:
        path = Path(self.upload_dir)
        return path if path.is_absolute() else BACKEND_DIR / path


settings = Settings()
