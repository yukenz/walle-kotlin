package id.co.awan.walle

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class KotlinGenericTest {


    inline fun <reified T> testReified() {

    }

    companion object {

        private val log = LoggerFactory.getLogger(this::class.java)

        class TypeErasure<T>(private val clazz: Class<T>) {

            private val items = mutableListOf<T>()

            fun addT(item: Any): Boolean {
                return if (clazz.isInstance(item)) {
                    @Suppress("UNCHECKED_CAST")
                    items.add(item as T)
                    true
                } else {
                    false
                }
            }

        }


    }


    @Test
    fun typeErasureCheck() {

        val listStr = listOf("a", "b", "c");
        val listInt = listOf(1, 2, 3)

        log.info("Object diff : {}", listStr.javaClass.equals(listInt.javaClass))

    }

    @Test
    fun typeErasureTest() {

        val typeErasure = TypeErasure(String::class.java)
        typeErasure.addT("a")
        typeErasure.addT(10)

    }

}
