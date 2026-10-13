from fastapi import APIRouter, Depends, Path, status
from sqlalchemy import delete, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.orm import Session

from app.db import get_db
from app.models import Favorite, User
from app.security import current_user

router = APIRouter(prefix="/favorites", tags=["즐겨찾기"])

# Keys follow FavoriteRepository in the app: "alert:<alertId>", "rescued:<animalId>".
_Key = Path(..., min_length=1, max_length=128)


@router.get("", response_model=list[str])
def favorite_keys(user: User = Depends(current_user), db: Session = Depends(get_db)):
    return list(db.scalars(select(Favorite.key).where(Favorite.user_id == user.id).order_by(Favorite.key)))


@router.put("/{key}", status_code=status.HTTP_204_NO_CONTENT)
def add_favorite(key: str = _Key, user: User = Depends(current_user), db: Session = Depends(get_db)):
    db.execute(insert(Favorite).values(user_id=user.id, key=key).on_conflict_do_nothing())
    db.commit()


@router.delete("/{key}", status_code=status.HTTP_204_NO_CONTENT)
def remove_favorite(key: str = _Key, user: User = Depends(current_user), db: Session = Depends(get_db)):
    db.execute(delete(Favorite).where(Favorite.user_id == user.id, Favorite.key == key))
    db.commit()
