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
                    <v-btn color="primary" dark @click="$emit('')">New Tournament</v-btn>
                </v-card-title>
            </template>

        </v-data-table>
    </v-card>

</template>

<script lang="ts">
    import { Component, Vue, Prop } from 'vue-property-decorator';
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

        newTournament() {
            this.currentTournament = new Tournament();
            this.editTournamentDialog = true;
        }

        //@Watch('editTournamentDialog')
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
