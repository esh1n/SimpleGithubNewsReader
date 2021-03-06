package com.lab.esh1n.github.domain.base

data class Resource<T>(val status: Status, val data: T?, val errorModel: ErrorModel?) {


    enum class Status {
        SUCCESS, ERROR, LOADING, ENDED
    }

    companion object {


        fun success(): Resource<Unit> {
            return Resource(Status.SUCCESS, Unit, null)
        }

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T, R> success(data: Resource<R>, func: (R?) -> T): Resource<T> {
            return Resource(Status.SUCCESS, func.invoke(data.data), data.errorModel)
        }

        fun <T> error(errorModel: ErrorModel): Resource<T> {
            return Resource(
                    Status.ERROR,
                    null,
                    errorModel
            )
        }

        fun <S, R> map(resource: Resource<S>, mapper: (S) -> R): Resource<R> {
            val data = resource.data
            if (resource.status == Status.SUCCESS) {
                val mappedData = if (data != null) mapper.invoke(data) else data
                return Resource(Status.SUCCESS, mappedData, resource.errorModel)
            }
            return copyWithNullData(resource)
        }

        fun <S, R> copyWithNullData(resource: Resource<S>): Resource<R> {
            return Resource(resource.status, null, resource.errorModel)
        }

        fun <T> loading(): Resource<T> {
            return Resource(
                    Status.LOADING,
                    null,
                    null
            )
        }

        fun <T> ended(): Resource<T> {
            return Resource(
                    Status.ENDED,
                    null,
                    null
            )
        }
    }
}
