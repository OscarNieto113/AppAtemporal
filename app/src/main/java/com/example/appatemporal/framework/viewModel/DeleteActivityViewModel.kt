package com.example.appatemporal.framework.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appatemporal.data.localdatabase.entities.Actividad
import com.example.appatemporal.data.requirements.TasksActivityRequirement
import com.example.appatemporal.domain.Repository
import kotlinx.coroutines.launch

class DeleteActivityViewModel : ViewModel(){
    private val requirement = TasksActivityRequirement()
    val activities = MutableLiveData<List<Actividad>>()

    fun removeActividad(id:Int, actividad: Actividad, repository: Repository){
        viewModelScope.launch {
            requirement.deleteActividad(actividad, repository)
            val list = requirement.getAllActivitiesId(id,repository)
            activities.setValue(list)
        }
    }
    fun updateActividad(idProyecto:Int, nombre:String, estatus:String, area:String, prioridad:String, id: Int, repository: Repository){
        viewModelScope.launch {
            requirement.updateActividad(nombre, estatus, area, prioridad, id, repository)
        }
    }

    fun getActivities(repository: Repository){
        viewModelScope.launch {
            val list = requirement.getActivities(repository)
            activities.setValue(list)

        }
    }
    fun getAllActivitiesid(id:Int, repository: Repository){
        viewModelScope.launch {
            val list = requirement.getAllActivitiesId(id,repository)
            activities.setValue(list)
        }
    }
}