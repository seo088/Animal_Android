import secrets

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app import schemas
from app.db import get_db
from app.models import User
from app.security import create_token, current_user, hash_password, verify_password

router = APIRouter(prefix="/auth", tags=["로그인"])


def _user_out(user: User) -> schemas.UserOut:
    return schemas.UserOut(id=user.id, display_name=user.display_name, email=user.email)


@router.post("/login", response_model=schemas.AuthResponse)
def login(body: schemas.LoginRequest, db: Session = Depends(get_db)):
    user = db.scalar(select(User).where(User.email == body.email.strip().lower()))
    if user is None or not verify_password(body.password, user.password_hash):
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.")
    return schemas.AuthResponse(token=create_token(user.id), user=_user_out(user))


@router.post("/signup", response_model=schemas.AuthResponse, status_code=status.HTTP_201_CREATED)
def sign_up(body: schemas.SignUpRequest, db: Session = Depends(get_db)):
    email = body.email.strip().lower()
    if db.scalar(select(User).where(User.email == email)) is not None:
        raise HTTPException(status.HTTP_409_CONFLICT, "이미 가입된 이메일입니다.")
    user = User(
        id="usr_" + secrets.token_hex(8),
        email=email,
        display_name=body.display_name.strip(),
        password_hash=hash_password(body.password),
    )
    db.add(user)
    db.commit()
    return schemas.AuthResponse(token=create_token(user.id), user=_user_out(user))


@router.get("/me", response_model=schemas.UserOut)
def me(user: User = Depends(current_user)):
    return _user_out(user)
