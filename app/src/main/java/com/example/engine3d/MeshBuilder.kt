package com.example.engine3d

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class MaterialStyle(
    val specular: Float,
    val shininess: Float,
    val alpha: Float = 1.0f,
    val emissive: Float = 0.0f
) {
    JCB_YELLOW(0.68f, 44f, 1f, 0f),
    MATTE_BLACK(0.22f, 14f, 1f, 0f),
    CAST_IRON(0.40f, 26f, 1f, 0f),
    CHROME_METAL(0.95f, 96f, 1f, 0f),
    BRASS_FITTING(0.75f, 56f, 1f, 0f),
    RUBBER_TYRE(0.12f, 8f, 1f, 0f),
    CAB_GLASS(0.85f, 80f, 0.42f, 0.05f),
    WORKSHOP_ENV(0.18f, 10f, 1f, 0f),
    SOIL_EARTH(0.08f, 6f, 1f, 0f),
    EMISSIVE_LAMP(0.5f, 32f, 1f, 0.75f)
}

class MeshData(
    val vertices: FloatArray, // stride 9: x, y, z, nx, ny, nz, r, g, b
    val indices: ShortArray,
    val style: MaterialStyle
) {
    val indexCount: Int = indices.size
    val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(vertices); position(0) }

    val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(indices.size * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply { put(indices); position(0) }
}

class MeshSubGroup(
    val meshes: List<MeshData>
)

class MeshBuilder(private val segments: Int = 16) {
    private val verts = ArrayList<Float>(1024)
    private val inds = ArrayList<Short>(1024)

    private fun pushVert(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float, r: Float, g: Float, b: Float): Short {
        val idx = (verts.size / 9).toShort()
        verts.add(x); verts.add(y); verts.add(z)
        verts.add(nx); verts.add(ny); verts.add(nz)
        verts.add(r); verts.add(g); verts.add(b)
        return idx
    }

    private fun pushQuad(i0: Short, i1: Short, i2: Short, i3: Short) {
        inds.add(i0); inds.add(i1); inds.add(i2)
        inds.add(i0); inds.add(i2); inds.add(i3)
    }

    fun addBox(cx: Float, cy: Float, cz: Float, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float) {
        addRotatedBox(cx, cy, cz, sx, sy, sz, 0f, 0f, 0f, r, g, b)
    }

    fun addRotatedBox(
        cx: Float, cy: Float, cz: Float,
        sx: Float, sy: Float, sz: Float,
        rxDeg: Float, ryDeg: Float, rzDeg: Float,
        r: Float, g: Float, b: Float
    ) {
        val hx = sx * 0.5f; val hy = sy * 0.5f; val hz = sz * 0.5f
        val mat = Mat4.trs(Vec3(cx, cy, cz), Vec3(rxDeg, ryDeg, rzDeg))

        fun xformPos(x: Float, y: Float, z: Float): Vec3 = Vec3(
            mat[0] * x + mat[4] * y + mat[8] * z + mat[12],
            mat[1] * x + mat[5] * y + mat[9] * z + mat[13],
            mat[2] * x + mat[6] * y + mat[10] * z + mat[14]
        )

        fun xformNrm(x: Float, y: Float, z: Float): Vec3 = Vec3(
            mat[0] * x + mat[4] * y + mat[8] * z,
            mat[1] * x + mat[5] * y + mat[9] * z,
            mat[2] * x + mat[6] * y + mat[10] * z
        ).normalized()

        val faces = arrayOf(
            // normal + 4 corners
            Pair(Vec3(0f, 0f, 1f), arrayOf(Vec3(-hx, -hy, hz), Vec3(hx, -hy, hz), Vec3(hx, hy, hz), Vec3(-hx, hy, hz))),
            Pair(Vec3(0f, 0f, -1f), arrayOf(Vec3(hx, -hy, -hz), Vec3(-hx, -hy, -hz), Vec3(-hx, hy, -hz), Vec3(hx, hy, -hz))),
            Pair(Vec3(1f, 0f, 0f), arrayOf(Vec3(hx, -hy, hz), Vec3(hx, -hy, -hz), Vec3(hx, hy, -hz), Vec3(hx, hy, hz))),
            Pair(Vec3(-1f, 0f, 0f), arrayOf(Vec3(-hx, -hy, -hz), Vec3(-hx, -hy, hz), Vec3(-hx, hy, hz), Vec3(-hx, hy, -hz))),
            Pair(Vec3(0f, 1f, 0f), arrayOf(Vec3(-hx, hy, hz), Vec3(hx, hy, hz), Vec3(hx, hy, -hz), Vec3(-hx, hy, -hz))),
            Pair(Vec3(0f, -1f, 0f), arrayOf(Vec3(-hx, -hy, -hz), Vec3(hx, -hy, -hz), Vec3(hx, -hy, hz), Vec3(-hx, -hy, hz)))
        )

        for ((n, corners) in faces) {
            val tn = xformNrm(n.x, n.y, n.z)
            val idxs = ShortArray(4) { i ->
                val p = xformPos(corners[i].x, corners[i].y, corners[i].z)
                pushVert(p.x, p.y, p.z, tn.x, tn.y, tn.z, r, g, b)
            }
            pushQuad(idxs[0], idxs[1], idxs[2], idxs[3])
        }
    }

    fun addCylinder(
        cx: Float, cy: Float, cz: Float,
        radiusTop: Float, radiusBot: Float, height: Float,
        axis: Int, // 0=X, 1=Y, 2=Z
        r: Float, g: Float, b: Float,
        segOverride: Int = segments
    ) {
        val seg = segOverride.coerceAtLeast(6)
        val halfH = height * 0.5f

        fun mapPt(u: Float, v: Float, h: Float): Vec3 = when (axis) {
            0 -> Vec3(cx + h, cy + u, cz + v)
            1 -> Vec3(cx + u, cy + h, cz + v)
            else -> Vec3(cx + u, cy + v, cz + h)
        }

        fun mapNrm(u: Float, v: Float, h: Float): Vec3 = when (axis) {
            0 -> Vec3(h, u, v).normalized()
            1 -> Vec3(u, h, v).normalized()
            else -> Vec3(u, v, h).normalized()
        }

        // Side wall
        val baseIdx = verts.size / 9
        for (i in 0..seg) {
            val a = (2.0 * PI * i / seg).toFloat()
            val ca = cos(a); val sa = sin(a)
            val n = mapNrm(ca, sa, 0f)
            val pb = mapPt(ca * radiusBot, sa * radiusBot, -halfH)
            val pt = mapPt(ca * radiusTop, sa * radiusTop, halfH)
            pushVert(pb.x, pb.y, pb.z, n.x, n.y, n.z, r, g, b)
            pushVert(pt.x, pt.y, pt.z, n.x, n.y, n.z, r, g, b)
        }
        for (i in 0 until seg) {
            val i0 = (baseIdx + i * 2).toShort()
            val i1 = (baseIdx + i * 2 + 1).toShort()
            val i2 = (baseIdx + (i + 1) * 2 + 1).toShort()
            val i3 = (baseIdx + (i + 1) * 2).toShort()
            pushQuad(i0, i3, i2, i1)
        }

        // Caps
        val topCenter = mapPt(0f, 0f, halfH)
        val topN = mapNrm(0f, 0f, 1f)
        val topCenterIdx = pushVert(topCenter.x, topCenter.y, topCenter.z, topN.x, topN.y, topN.z, r, g, b)
        val topRingStart = verts.size / 9
        for (i in 0..seg) {
            val a = (2.0 * PI * i / seg).toFloat()
            val p = mapPt(cos(a) * radiusTop, sin(a) * radiusTop, halfH)
            pushVert(p.x, p.y, p.z, topN.x, topN.y, topN.z, r, g, b)
        }
        for (i in 0 until seg) {
            inds.add(topCenterIdx); inds.add((topRingStart + i).toShort()); inds.add((topRingStart + i + 1).toShort())
        }

        val botCenter = mapPt(0f, 0f, -halfH)
        val botN = mapNrm(0f, 0f, -1f)
        val botCenterIdx = pushVert(botCenter.x, botCenter.y, botCenter.z, botN.x, botN.y, botN.z, r, g, b)
        val botRingStart = verts.size / 9
        for (i in 0..seg) {
            val a = (2.0 * PI * i / seg).toFloat()
            val p = mapPt(cos(a) * radiusBot, sin(a) * radiusBot, -halfH)
            pushVert(p.x, p.y, p.z, botN.x, botN.y, botN.z, r, g, b)
        }
        for (i in 0 until seg) {
            inds.add(botCenterIdx); inds.add((botRingStart + i + 1).toShort()); inds.add((botRingStart + i).toShort())
        }
    }

    fun addTube(points: List<Vec3>, radius: Float, r: Float, g: Float, b: Float) {
        if (points.size < 2) return
        val seg = (segments / 2).coerceAtLeast(6)
        val ringStarts = IntArray(points.size)
        for (pi in points.indices) {
            val tangent = when (pi) {
                0 -> (points[1] - points[0]).normalized()
                points.lastIndex -> (points[pi] - points[pi - 1]).normalized()
                else -> (points[pi + 1] - points[pi - 1]).normalized()
            }
            val up = if (kotlin.math.abs(tangent.y) < 0.9f) Vec3(0f, 1f, 0f) else Vec3(1f, 0f, 0f)
            val right = tangent.cross(up).normalized()
            val binorm = right.cross(tangent).normalized()

            ringStarts[pi] = verts.size / 9
            for (s in 0..seg) {
                val a = (2.0 * PI * s / seg).toFloat()
                val n = (right * cos(a) + binorm * sin(a)).normalized()
                val p = points[pi] + n * radius
                pushVert(p.x, p.y, p.z, n.x, n.y, n.z, r, g, b)
            }
        }
        for (pi in 0 until points.lastIndex) {
            val r0 = ringStarts[pi]
            val r1 = ringStarts[pi + 1]
            for (s in 0 until seg) {
                pushQuad(
                    (r0 + s).toShort(),
                    (r0 + s + 1).toShort(),
                    (r1 + s + 1).toShort(),
                    (r1 + s).toShort()
                )
            }
        }
    }

    fun addBolt(cx: Float, cy: Float, cz: Float, radius: Float, length: Float, axis: Int) {
        addCylinder(cx, cy, cz, radius * 1.55f, radius * 1.55f, length * 0.35f, axis, 0.85f, 0.88f, 0.92f, 6)
        addCylinder(cx, cy, cz, radius, radius, length, axis, 0.65f, 0.68f, 0.72f, 6)
    }

    fun addTorusRing(
        cx: Float, cy: Float, cz: Float,
        majorRadius: Float, tubeRadius: Float,
        axis: Int, // 1=Y (horizontal ring), 2=Z (vertical ring facing Z)
        r: Float, g: Float, b: Float,
        gapDeg: Float = 0f
    ) {
        val steps = (segments.coerceAtLeast(16))
        val spanRad = ((360f - gapDeg) * PI / 180.0)
        val startRad = (gapDeg * 0.5f * PI / 180.0)
        val pts = ArrayList<Vec3>(steps + 1)
        for (i in 0..steps) {
            val a = (startRad + spanRad * (i.toDouble() / steps)).toFloat()
            val ca = cos(a) * majorRadius
            val sa = sin(a) * majorRadius
            val pt = when (axis) {
                1 -> Vec3(cx + ca, cy, cz + sa)
                0 -> Vec3(cx, cy + ca, cz + sa)
                else -> Vec3(cx + ca, cy + sa, cz)
            }
            pts.add(pt)
        }
        addTube(pts, tubeRadius, r, g, b)
    }

    fun addHelixCoil(
        cx: Float, cy: Float, cz: Float,
        radius: Float, wireRadius: Float, height: Float, turns: Float,
        r: Float, g: Float, b: Float
    ) {
        val totalPts = (turns * 14).toInt().coerceAtLeast(24)
        val pts = ArrayList<Vec3>(totalPts + 1)
        for (i in 0..totalPts) {
            val t = i.toFloat() / totalPts
            val a = (t * turns * 2f * PI.toFloat())
            pts.add(Vec3(cx + cos(a) * radius, cy - height * 0.5f + t * height, cz + sin(a) * radius))
        }
        addTube(pts, wireRadius, r, g, b)
    }

    fun addHalfShell(
        cx: Float, cy: Float, cz: Float,
        radius: Float, width: Float, thickness: Float,
        isUpper: Boolean,
        r: Float, g: Float, b: Float
    ) {
        val arcSteps = 10
        val startAngle = if (isUpper) 0f else PI.toFloat()
        for (i in 0 until arcSteps) {
            val a = startAngle + ((i + 0.5f) / arcSteps) * PI.toFloat()
            val deg = Math.toDegrees(a.toDouble()).toFloat()
            val px = cx + cos(a) * radius
            val py = cy + sin(a) * radius
            addRotatedBox(px, py, cz, thickness, (PI.toFloat() * radius / arcSteps) * 1.08f, width, 0f, 0f, deg, r, g, b)
        }
    }

    fun build(style: MaterialStyle): MeshData =
        MeshData(verts.toFloatArray(), inds.toShortArray(), style)
}
