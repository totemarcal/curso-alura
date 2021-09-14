package br.com.alura.technews.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.alura.technews.model.Noticia
import br.com.alura.technews.repository.NoticiaRepository
import br.com.alura.technews.repository.Resource

class FormularioNoticiaViewModel(
    private val repository: NoticiaRepository

): ViewModel() {
    fun salvar(noticia: Noticia) : LiveData<Resource<Void?>>{
        return repository.salva(noticia)
    }

    fun editar(noticia: Noticia) : LiveData<Resource<Void?>>{
        return repository.edita(noticia)
    }

}