package com.example.datastore

import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.datastore.CorruptionException
import androidx.datastore.DataStore
import androidx.datastore.Serializer
import androidx.datastore.createDataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.example.datastore.databinding.ActivityMainBinding
import com.example.datastore.model.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.*

//https://blog.untitledkingdom.com/refactoring-recyclerview-adapter-to-data-binding-5631f239095f
//https://medium.com/better-programming/recyclerview-expanded-1c1be424282c

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ExpendableRecyclerViewAdapter
    private lateinit var binding: ActivityMainBinding

    // declare value for working with datastore
    lateinit var dataStore: DataStore<Preferences>

    //create key for save data type
    val PREF_UUID = preferencesKey<String>(name = "uuid")

    // declare value for receive data
    lateinit var uuid: Flow<String>

    //create proto data store
    lateinit var protoDataStore: DataStore<TestModel>
    lateinit var token: Flow<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //create preference data store
        dataStore = this.createDataStore(name = "MyPreferenceDataStore")

        //create proto data store
        protoDataStore = this.createDataStore(
            fileName = "MyProtoDataStore.proto",
            serializer = MyProtoDataStoreSerializer
        )

        readUUID()
        CoroutineScope(Dispatchers.Main).launch {
            uuid.collect { value ->
                if (value.isEmpty()) {
                    val newUUID = UUID.randomUUID().toString()
                    showToast("Not have UUID saved. Create new UUID ${newUUID} and save to Preferences DataStore")
                    delay(5000)
                    saveUUID(newUUID)

                } else {
                    showToast("Read UUID from preference data store ${value}")
                }
            }
        }

        readToken()
        CoroutineScope(Dispatchers.Main).launch {
            token.collect { value ->
                if (value.isEmpty()) {
                    val newToken = "Your token ABCDEF"
                    showToast("Not have token saved. Create new token ${newToken} and save to Proto DataStore")
                    delay(50000)
                    saveToken(newToken)

                } else {
                    showToast("Read the token from proto data store ${value}")
                }
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        showListPerson()
    }

    private fun showToast(s: String) {
        this.runOnUiThread {
            val toast = Toast.makeText(this, s, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    private fun showListPerson() {
        adapter = ExpendableRecyclerViewAdapter(getListPersonFake())
        binding.recyclerPerson.adapter = adapter
        binding.recyclerPerson.itemAnimator = null
    }

    fun getListPersonFake(): List<Person> {
        val people = mutableListOf<Person>()

        val personNames = resources.getStringArray(R.array.people)
        val images = resources.obtainTypedArray(R.array.images)

        val generalDescription = resources.getString(R.string.description)
        personNames.forEachIndexed { index, personName ->
            val person = Person(
                personName,
                generalDescription,
                images.getResourceId(index, -1),
                false
            )
            people.add(person)
        }

        return people
    }

    suspend fun saveUUID(uuid: String) {
        dataStore.edit {
            it[PREF_UUID] = uuid
        }
    }

    fun readUUID() {
        uuid = dataStore.data.map {
            it[PREF_UUID] ?: ""
        }
    }

    object MyProtoDataStoreSerializer : Serializer<TestModel> {
        override fun readFrom(input: InputStream): TestModel {
            try {
                return TestModel.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }

        override fun writeTo(t: TestModel, output: OutputStream) = t.writeTo(output)
    }


    suspend fun saveToken(token: String) {
        protoDataStore.updateData {
            it.toBuilder().setToken(token).build()
        }
    }

    fun readToken() {
        token = protoDataStore.data.map {
            it.token
        }
    }
}


