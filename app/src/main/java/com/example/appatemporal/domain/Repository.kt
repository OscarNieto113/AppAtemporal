package com.example.appatemporal.domain

import android.content.Context
import com.example.appatemporal.data.localdatabase.LocalDatabase
import com.example.appatemporal.data.localdatabase.entities.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import com.example.appatemporal.domain.models.UserModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class Repository(context: Context) {
    val firestoreAPI = FirestoreService()

    suspend fun addUser(uid: String, user: UserModel, role: String) {
        firestoreAPI.addUser(uid, user)
        firestoreAPI.addUserRole(uid, role)
    }

    suspend fun verifyUser(uid: String) : Boolean {
        return firestoreAPI.verifyUser(uid)
    }

    suspend fun getUser(uid: String) : DocumentSnapshot{
        return firestoreAPI.getUser(uid)
    }

    suspend fun getUserRole(uid: String) : DocumentSnapshot {
        return firestoreAPI.getUserRole(uid)
    }

    suspend fun eventCount(uid: String) : Int {
        return firestoreAPI.eventCount(uid)
    }

    suspend fun ventasCount(uid: String) : Pair<Int, Int> {
        return firestoreAPI.ventasCount(uid)
    }

    suspend fun getRating(uid: String) : Float {
        return firestoreAPI.getRating(uid)
    }

    suspend fun getRevenue(uid: String) : Int {
        return firestoreAPI.getRevenue(uid)
    }

    suspend fun updateTicketValue(resulted: String) : Boolean {
        return firestoreAPI.updateTicketValue(resulted)
    }

    // Local database
    val actividadDao = LocalDatabase.getInstance(context).actividadDao
    val areaDao = LocalDatabase.getInstance(context).areaDao
    val estatusDao = LocalDatabase.getInstance(context).estatusDao
    val objetivoDao = LocalDatabase.getInstance(context).objetivoDao
    val proyectoDao = LocalDatabase.getInstance(context).proyectoDao
    val usuarioDao = LocalDatabase.getInstance(context).usuarioDao
    val privilegioDao = LocalDatabase.getInstance(context).privilegioDao
    val rolDao = LocalDatabase.getInstance(context).rolDao

    suspend fun insertActividad(actividad: Actividad) = actividadDao.insert(actividad)
    suspend fun insertAllActividades(actividades: List<Actividad>) = actividadDao.insertAll(actividades)
    suspend fun getAllActividades() = actividadDao.getAll()
    suspend fun getActividadById(id: Int) = actividadDao.getById(id)
    suspend fun getAllActividadById(id: Int) = actividadDao.getAllActivityId(id)
    suspend fun deleteActividad(actividad: Actividad) = actividadDao.delete(actividad)
    suspend fun deleteAllActividades() = actividadDao.deleteAll()
    fun countPendingActivities(id_a: Int, stringStatus: String): Int = runBlocking {
        val count = async {
            actividadDao.countPendingActivities(id_a, stringStatus)
        }
        count.start()
        count.await()
    }

    fun countDoneActivities(id_a: Int, stringStatus: String): Int = runBlocking {
        val count = async {
            actividadDao.countDoneActivities(id_a, stringStatus)
        }
        count.start()
        count.await()
    }
    suspend fun updateActividad(nombre:String, estatus:String, area:String, prioridad:String, id: Int) = actividadDao.update(nombre, estatus, area, prioridad, id)

    suspend fun insertArea(area: Area) = areaDao.insert(area)
    suspend fun insertAllAreas(areas: List<Area>) = areaDao.insertAll(areas)
    suspend fun getAllAreas() = areaDao.getAll()
    suspend fun getAreaById(id: Int) = areaDao.getById(id)
    suspend fun deleteArea(area: Area) = areaDao.delete(area)
    suspend fun deleteAllAreas() = areaDao.deleteAll()

    suspend fun insertEstatus(estatus: Estatus) = estatusDao.insert(estatus)
    suspend fun insertAllEstatus(estatus: List<Estatus>) = estatusDao.insertAll(estatus)
    suspend fun getAllEstatus() = estatusDao.getAll()
    suspend fun getEstatusById(id: Int) = estatusDao.getById(id)
    suspend fun deleteEstatus(estatus: Estatus) = estatusDao.delete(estatus)
    suspend fun deleteAllEstatus() = estatusDao.deleteAll()

    suspend fun insertObjetivo(objetivo: Objetivo) = objetivoDao.insert(objetivo)
    suspend fun insertAllObjetivos(objetivos: List<Objetivo>) = objetivoDao.insertAll(objetivos)
    suspend fun getAllObjetivos() = objetivoDao.getAll()
    suspend fun getObjetivoById(id: Int) = objetivoDao.getById(id)
    suspend fun deleteObjetivo(objetivo: Objetivo) = objetivoDao.delete(objetivo)
    suspend fun deleteAllObjetivos() = objetivoDao.deleteAll()


    suspend fun insertProyecto(proyecto: Proyecto) = proyectoDao.insert(proyecto)
    suspend fun insertAllProyectos(proyectos: List<Proyecto>) = proyectoDao.insertAll(proyectos)
    suspend fun getAllProyectos() = proyectoDao.getAll()
    suspend fun getProyectoById(id: Int) = proyectoDao.getById(id)
    suspend fun deleteProyecto(proyecto: Proyecto) = proyectoDao.delete(proyecto)
    suspend fun deleteAllProyectos() = proyectoDao.deleteAll()
    suspend fun updateProyecto(proyecto: Proyecto) = proyectoDao.update(proyecto)
    suspend fun updatePresupuesto(presupuestoN:Double, id: Int) =proyectoDao.updatePresupuesto(presupuestoN,id)
    suspend fun updateMeta(metaN:Double, id: Int) =proyectoDao.updateMeta(metaN,id)
    suspend fun updateModifyProyect(name: String, date: String,time: String, id: Int) =proyectoDao.updateModify(name,date,time,id)

    suspend fun addUserLocalDB(user: Usuario) = usuarioDao.insertUserLocalDB(user)
    suspend fun getUserLocalDB(userUid: String) : Usuario = usuarioDao.getUserLocalDB(userUid)
}