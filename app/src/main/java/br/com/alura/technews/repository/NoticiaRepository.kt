package br.com.alura.technews.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.alura.technews.asynctask.BaseAsyncTask
import br.com.alura.technews.database.dao.NoticiaDAO
import br.com.alura.technews.model.Noticia
import br.com.alura.technews.retrofit.webclient.NoticiaWebClient

class NoticiaRepository(
    private val dao: NoticiaDAO,
    private val webclient: NoticiaWebClient = NoticiaWebClient()
) {

    private val noticiasEncontradas = MutableLiveData<Resource<List<Noticia>?>>()

    fun buscaTodos() : LiveData<Resource<List<Noticia>?>> {
        val atualizaListaNoticias: (List<Noticia>) -> Unit = {
            noticiasEncontradas.value = Resource(dado = it)
        }
        buscaInterno(quandoSucesso= atualizaListaNoticias)
        buscaNaApi(quandoSucesso = atualizaListaNoticias, quandoFalha = { erro ->
            val resourceAtual = noticiasEncontradas.value
            val resourceFalha = criaResourceFalha<List<Noticia>?>(resourceAtual, erro)
            noticiasEncontradas.value = resourceFalha
        })
        return noticiasEncontradas
    }



    fun salva(
        noticia: Noticia
    ) : LiveData<Resource<Void?>>{
        val liveData = MutableLiveData<Resource<Void?>>()
        salvaNaApi(noticia, quandoSucesso = {
            liveData.value = Resource(dado = null)
        }, quandoFalha = { erro ->
            liveData.value = Resource(dado = null, erro = erro)
        })
        return liveData
    }

    fun remove(
        noticia: Noticia,
        quandoSucesso: () -> Unit,
        quandoFalha: (erro: String?) -> Unit
    ) {
        removeNaApi(noticia, quandoSucesso, quandoFalha)
    }

    fun edita(
        noticia: Noticia
    ) : LiveData<Resource<Void?>>{
        val liveData = MutableLiveData<Resource<Void?>>()
        editaNaApi(noticia, quandoSucesso = {
            liveData.value = Resource(dado=null)
        }, quandoFalha = { erro ->
            liveData.value = Resource(dado = null, erro = erro)
        })
        return liveData
    }

    fun buscaPorId(
        noticiaId: Long,
        quandoSucesso: (noticiaEncontrada: Noticia?) -> Unit
    ) {
        BaseAsyncTask(quandoExecuta = {
            dao.buscaPorId(noticiaId)
        }, quandoFinaliza = quandoSucesso)
            .execute()
    }

    private fun buscaNaApi(
        quandoSucesso: (List<Noticia>) -> Unit,
        quandoFalha: (erro: String?) -> Unit
    ) {
        webclient.buscaTodas(
            quandoSucesso = { noticiasNovas ->
                noticiasNovas?.let {
                    salvaInterno(noticiasNovas, quandoSucesso)
                }
            }, quandoFalha = quandoFalha
        )
    }

    private fun buscaInterno(quandoSucesso: (List<Noticia>) -> Unit) {
        BaseAsyncTask(quandoExecuta = {
            dao.buscaTodos()
        }, quandoFinaliza = quandoSucesso)
            .execute()
    }


    private fun salvaNaApi(
        noticia: Noticia,
        quandoSucesso: (noticiaNova: Noticia) -> Unit,
        quandoFalha: (erro: String?) -> Unit
    ) {
        webclient.salva(
            noticia,
            quandoSucesso = {
                it?.let { noticiaSalva ->
                    salvaInterno(noticiaSalva, quandoSucesso)
                }
            }, quandoFalha = quandoFalha
        )
    }

    private fun salvaInterno(
        noticias: List<Noticia>,
        quandoSucesso: (noticiasNovas: List<Noticia>) -> Unit
    ) {
        BaseAsyncTask(
            quandoExecuta = {
                dao.salva(noticias)
                dao.buscaTodos()
            }, quandoFinaliza = quandoSucesso
        ).execute()
    }

    private fun salvaInterno(
        noticia: Noticia,
        quandoSucesso: (noticiaNova: Noticia) -> Unit
    ) {
        BaseAsyncTask(quandoExecuta = {
            dao.salva(noticia)
            dao.buscaPorId(noticia.id)
        }, quandoFinaliza = { noticiaEncontrada ->
            noticiaEncontrada?.let {
                quandoSucesso(it)
            }
        }).execute()

    }

    private fun removeNaApi(
        noticia: Noticia,
        quandoSucesso: () -> Unit,
        quandoFalha: (erro: String?) -> Unit
    ) {
        webclient.remove(
            noticia.id,
            quandoSucesso = {
                removeInterno(noticia, quandoSucesso)
            },
            quandoFalha = quandoFalha
        )
    }


    private fun removeInterno(
        noticia: Noticia,
        quandoSucesso: () -> Unit
    ) {
        BaseAsyncTask(quandoExecuta = {
            dao.remove(noticia)
        }, quandoFinaliza = {
            quandoSucesso()
        }).execute()
    }

    private fun editaNaApi(
        noticia: Noticia,
        quandoSucesso: (noticiaEditada: Noticia) -> Unit,
        quandoFalha: (erro: String?) -> Unit
    ) {
        webclient.edita(
            noticia.id, noticia,
            quandoSucesso = { noticiaEditada ->
                noticiaEditada?.let {
                    salvaInterno(noticiaEditada, quandoSucesso)
                }
            }, quandoFalha = quandoFalha
        )
    }

}
