package id.co.awan.walle

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.properties.ReadWriteProperty

import kotlin.reflect.KProperty

class KotlinStringDelegateTest {

    companion object {

        private val log = LoggerFactory.getLogger(this::class.java)


        class StringDelegate<T> (clazz: Class<T>) : ReadWriteProperty<Any?, T> {

            var value: T = clazz.getDeclaredConstructor().newInstance()

            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                log.info("Getting value of {}", property.name)
                log.info("getValue thisRef : {}, property : {}, value : {}, ", thisRef?.javaClass?.name, property.name, value)
                return value
            }

            override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                log.info("Setting {} to {}", property.name, value)
                log.info("setValue thisRef : {}, property : {}, value : {}, ", thisRef?.javaClass?.name, property.name, value)
                this.value = value
            }
        }
    }



}