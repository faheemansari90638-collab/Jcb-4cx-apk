package com.example.engine3d

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun normalized(): Vec3 {
        val len = length()
        return if (len > 1e-5f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }
    fun dot(o: Vec3): Float = x * o.x + y * o.y + z * o.z
    fun cross(o: Vec3): Vec3 = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )
    fun distanceTo(o: Vec3): Float = (this - o).length()
}

object Mat4 {
    fun identity(): FloatArray = FloatArray(16).apply {
        this[0] = 1f; this[5] = 1f; this[10] = 1f; this[15] = 1f
    }

    fun multiply(a: FloatArray, b: FloatArray): FloatArray {
        val out = FloatArray(16)
        for (col in 0..3) {
            for (row in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += a[k * 4 + row] * b[col * 4 + k]
                }
                out[col * 4 + row] = sum
            }
        }
        return out
    }

    fun translation(x: Float, y: Float, z: Float): FloatArray = identity().apply {
        this[12] = x; this[13] = y; this[14] = z
    }

    fun scale(sx: Float, sy: Float, sz: Float): FloatArray = identity().apply {
        this[0] = sx; this[5] = sy; this[10] = sz
    }

    fun rotationX(deg: Float): FloatArray {
        val r = Math.toRadians(deg.toDouble()).toFloat()
        val c = cos(r); val s = sin(r)
        return identity().apply {
            this[5] = c; this[6] = s
            this[9] = -s; this[10] = c
        }
    }

    fun rotationY(deg: Float): FloatArray {
        val r = Math.toRadians(deg.toDouble()).toFloat()
        val c = cos(r); val s = sin(r)
        return identity().apply {
            this[0] = c; this[2] = -s
            this[8] = s; this[10] = c
        }
    }

    fun rotationZ(deg: Float): FloatArray {
        val r = Math.toRadians(deg.toDouble()).toFloat()
        val c = cos(r); val s = sin(r)
        return identity().apply {
            this[0] = c; this[1] = s
            this[4] = -s; this[5] = c
        }
    }

    fun trs(pos: Vec3, rotDeg: Vec3, scl: Vec3 = Vec3(1f, 1f, 1f)): FloatArray {
        val t = translation(pos.x, pos.y, pos.z)
        val ry = rotationY(rotDeg.y)
        val rx = rotationX(rotDeg.x)
        val rz = rotationZ(rotDeg.z)
        val s = scale(scl.x, scl.y, scl.z)
        return multiply(t, multiply(ry, multiply(rx, multiply(rz, s))))
    }

    fun perspective(fovDeg: Float, aspect: Float, near: Float, far: Float): FloatArray {
        val f = 1f / kotlin.math.tan(Math.toRadians((fovDeg * 0.5f).toDouble())).toFloat()
        val out = FloatArray(16)
        out[0] = f / aspect
        out[5] = f
        out[10] = (far + near) / (near - far)
        out[11] = -1f
        out[14] = (2f * far * near) / (near - far)
        return out
    }

    fun lookAt(eye: Vec3, center: Vec3, up: Vec3): FloatArray {
        val f = (center - eye).normalized()
        val s = f.cross(up).normalized()
        val u = s.cross(f)
        return floatArrayOf(
            s.x, u.x, -f.x, 0f,
            s.y, u.y, -f.y, 0f,
            s.z, u.z, -f.z, 0f,
            -s.dot(eye), -u.dot(eye), f.dot(eye), 1f
        )
    }

    fun angleDiffDeg(a: Float, b: Float): Float {
        var d = ((a - b) % 360f + 540f) % 360f - 180f
        if (d < -180f) d += 360f
        return abs(d)
    }
}
