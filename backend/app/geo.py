import math

from sqlalchemy import ColumnElement, and_, func

EARTH_RADIUS_KM = 6371.0


def distance_km(lat_col, lng_col, lat: float, lng: float) -> ColumnElement[float]:
    """Great-circle distance in SQL (spherical law of cosines, clamped for acos)."""
    cos_angle = (
        func.cos(func.radians(lat)) * func.cos(func.radians(lat_col))
        * func.cos(func.radians(lng_col) - func.radians(lng))
        + func.sin(func.radians(lat)) * func.sin(func.radians(lat_col))
    )
    return EARTH_RADIUS_KM * func.acos(func.greatest(-1.0, func.least(1.0, cos_angle)))


def bounding_box(lat_col, lng_col, lat: float, lng: float, radius_km: float) -> ColumnElement[bool]:
    """Cheap index-friendly prefilter before the exact distance check."""
    dlat = radius_km / 111.0
    dlng = radius_km / (111.0 * max(math.cos(math.radians(lat)), 0.01))
    return and_(lat_col.between(lat - dlat, lat + dlat), lng_col.between(lng - dlng, lng + dlng))
