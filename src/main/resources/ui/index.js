new Vue({
    el: '#app',
    data() {
        return {
            portfolio: null,
            loading: true,
            error: null
        }
    },
    mounted() {
        fetch('http://localhost:8080/portfolio/key/test1?version=1')
            .then(response => {
                return response.json()
            })
            .then(json => {
                this.portfolio = json
            })
            .catch(error => {
                console.log(error)
                this.error = error
            })
            .finally(() => this.loading = false)
    }
});

Vue.filter('currency', function(value) {
    if (!value) {
        return '';
    }

    return value.toLocaleString(navigator.language, { style: 'currency', currency: 'USD' })
});

Vue.filter('decimal', function(value) {
    if (!value) {
        return '';
    }

    return value.toLocaleString(navigator.language, { style: 'decimal' })
});
