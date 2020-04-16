<template>

    <v-card class="table">
        <h1>Created Tournaments</h1>

        <v-data-table
                :headers="headers"
                :items="createdTournaments"
                :search="search"
                multi-sort
                :mobile-breakpoint="0"
                :items-per-page="15"
                :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
                no-data-text = "No Available Tournaments"
                no-results-text = "No Tournaments Found"
        >

            <template v-slot:top>
                <v-card-title>
                    <v-text-field
                            v-model="search"
                            append-icon="search"
                            label="Search"
                            class="mx-2"
                    />

                    <v-spacer />
                    <v-btn color="primary" dark @click="newTournament">New Tournament</v-btn>
                    <edit-tournament-dialog
                            v-if="currentTournament"
                            v-model="editTournamentDialog"
                            :tournament="currentTournament"
                            :topics="topics"
                            @saveTournament="createdTournament"
                    />
                </v-card-title>
            </template>

        </v-data-table>
    </v-card>

</template>

<script lang="ts">
    import { Component, Vue, Watch } from 'vue-property-decorator';
    import Tournament from "@/models/management/Tournament";
    import RemoteServices from '@/services/RemoteServices';
    import Topic from '@/models/management/Topic';
    import EditTournamentDialog from '@/views/student/tournament/EditTournamentDialog.vue';

    @Component({
        components: {
            'edit-tournament-dialog': EditTournamentDialog
        }
    })

    export default class CreatedTournamentsView extends Vue {
        currentTournament: Tournament | null = null;
        editTournamentDialog: boolean = false;
        topics!: Topic[];

        headers: object = [
            { text: 'Title',
                value: 'title',
                align: 'center',
                width: '20%'
            },
            {
                text: 'Topics',
                value: 'topics',
                align: 'center',
                width: '20%',
                sortable: false
            },
            {
                text: 'Nº of Questions',
                value: 'numberOfQuestions',
                align: 'center',
                width: '10%'
            },
            {
                text: 'Status',
                value: 'status',
                align: 'center',
                width: '10%',
            },
            {
                text: 'Actions',
                value: 'actions',
                align: 'center',
                width: '10%',
                sortable: false
            },
        ];

        newTournament() {
            this.currentTournament = new Tournament();
            this.editTournamentDialog = true;
        }

        @Watch('editTournamentDialog')
        closeError() {
            if (!this.editTournamentDialog) {
                this.currentTournament = null;
            }
        }

        createdTournament(tournament: Tournament) {
            this.editTournamentDialog = false;
        }

        async created() {
            await this.$store.dispatch('loading');
            try {
                this.topics = await RemoteServices.getTopics();
            } catch (error) {
                await this.$store.dispatch('error', error);
            }
            await this.$store.dispatch('clearLoading');
        }
    }
</script>

<style lang="scss" scoped></style>
